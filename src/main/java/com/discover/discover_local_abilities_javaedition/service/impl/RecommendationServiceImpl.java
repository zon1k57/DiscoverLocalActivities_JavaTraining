package com.discover.discover_local_abilities_javaedition.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.discover.discover_local_abilities_javaedition.dto.ActivityWithHoursDTO;
import com.discover.discover_local_abilities_javaedition.dto.RecommendationsDTO;
import com.discover.discover_local_abilities_javaedition.dto.WorkingHoursDTO;
import com.discover.discover_local_abilities_javaedition.dto.RecommendedActivitiesDTO;
import com.discover.discover_local_abilities_javaedition.model.User;
import com.discover.discover_local_abilities_javaedition.repository.UserRepository;
import com.discover.discover_local_abilities_javaedition.service.ActivityService;
import com.discover.discover_local_abilities_javaedition.service.RecommendationService;

@Service
class RecommendationServiceImpl implements RecommendationService {
  private final ActivityService activityService;
  private final UserRepository userRepository;

  public RecommendationServiceImpl(ActivityService activityService, UserRepository userRepository){
    this.activityService = activityService;
    this.userRepository = userRepository;
  }

  private static final double DISTANCE_WEIGHT = 0.35;
  private static final double RATING_WEIGHT = 0.3;
  private static final double POPULARITY_WEIGHT = 0.2;
  private static final double RELEVANCE_WEIGHT = 0.15;

  // Haversine formula (returns kilometers)
  private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    final double R = 6371.0; // Earth radius km
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon/2) * Math.sin(dLon/2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  }

  private boolean isOpen(ActivityWithHoursDTO activity, LocalDateTime now) {
    if (activity.getWorkingHours() == null || activity.getWorkingHours().isEmpty()) {
      return true; // assume open if no hours provided
    }

    DayOfWeek dow = now.getDayOfWeek();
    String currentDay = dow.toString().toLowerCase();
    LocalTime currentTime = now.atZone(ZoneId.systemDefault()).toLocalTime();

    for (WorkingHoursDTO wh : activity.getWorkingHours()) {
      if (wh.getDayOfWeek() == null) continue;
      String wday = wh.getDayOfWeek().trim().toLowerCase();
      if (!wday.equals(currentDay)) continue;
      if (wh.isClosed()) return false;
      if (wh.is24h()) return true;

      LocalTime open = wh.getOpenTime();
      LocalTime close = wh.getClosedTime();
      if (open == null || close == null) continue;

      boolean inRange;
      if (!open.isAfter(close)) {
        inRange = !currentTime.isBefore(open) && !currentTime.isAfter(close);
      } else {
        // overnight
        inRange = !currentTime.isBefore(open) || !currentTime.isAfter(close);
      }

      if (!inRange) continue;

      LocalTime breakStart = wh.getBreakTimeStart();
      LocalTime breakEnd = wh.getBreakTimeEnd();
      if (breakStart != null && breakEnd != null) {
        boolean inBreak;
        if (!breakStart.isAfter(breakEnd)) {
          inBreak = !currentTime.isBefore(breakStart) && !currentTime.isAfter(breakEnd);
        } else {
          inBreak = !currentTime.isBefore(breakStart) || !currentTime.isAfter(breakEnd);
        }
        if (inBreak) continue;
      }

      return true;
    }

    return false;
  }

  private double clamp(double v, double min, double max) {
    return Math.max(min, Math.min(max, v));
  }

  private double distanceScore(double distanceKm, Double radiusKm) {
    if (radiusKm == null || radiusKm <= 0) return 0.0;
    return clamp(1.0 - (distanceKm / radiusKm), 0.0, 1.0);
  }

  private double ratingScore(Double rating) {
    if (rating == null) return 0.0;
    return clamp(rating / 5.0, 0.0, 1.0);
  }

  private double popularityScore(Integer userRatingCount) {
    int cnt = userRatingCount == null ? 0 : userRatingCount;
    double score = Math.log10((double)cnt + 1.0) / 4.0;
    return Math.min(score, 1.0);
  }

  private double categoryRelevance(String activityType, String context) {
    if ("general".equalsIgnoreCase(context)) return 0.0;
    if (activityType == null) return 0.0;

    // Use Constants mapping to compute relevance rank-based score
    var ctx = context.trim().toLowerCase();
    var normalizedType = activityType.trim().toLowerCase();

    var mapping = com.discover.discover_local_abilities_javaedition.service.helper.Constants.CONTEXT_ACTIVITY_TYPES;

    List<String> contextTypes = mapping.get(ctx);
    if (contextTypes == null) {
      // Unknown context -> no relevance
      return 0.0;
    }

    int idx = -1;
    for (int i = 0; i < contextTypes.size(); i++) {
      if (contextTypes.get(i).equalsIgnoreCase(normalizedType)) {
        idx = i;
        break;
      }
    }

    if (idx == -1) return 0.0;
    if (contextTypes.size() == 1) return 1.0;

    double minRel = com.discover.discover_local_abilities_javaedition.service.helper.Constants.MIN_CATEGORY_RELEVANCE;
    double step = (1.0 - minRel) / (contextTypes.size() - 1);
    double score = 1.0 - (idx * step);
    score = Math.max(score, minRel);
    // round to 3 decimal places to match python behavior
    return Math.round(score * 1000.0) / 1000.0;
  }

  private double recommendationScore(double distanceScore, double ratingScore, double popularityScore, double relevance) {
    double total = distanceScore * DISTANCE_WEIGHT
        + ratingScore * RATING_WEIGHT
        + popularityScore * POPULARITY_WEIGHT
        + relevance * RELEVANCE_WEIGHT;
    return Math.round(total * 1000.0) / 1000.0;
  }

  @Override
  public List<RecommendationsDTO> findByUserId(Long id, Double radiusKm, String context) {
    User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User id not found " + id));
    return findByChoords(user.getLatitude(), user.getLongitude(), radiusKm, context);
  }

  @Override
  public List<RecommendationsDTO> findByChoords(Double lat, Double lon, Double radiusKm, String context) {
    List<ActivityWithHoursDTO> activities = activityService.findNearby(lat, lon, radiusKm, context);
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

    if (radiusKm == null){
      radiusKm = 1.0;
    }

    if (context == null || context.isEmpty()){
      context = "general";
    }

    class Ranked {
      ActivityWithHoursDTO a;
      double distanceKm;
      double score;
      double relevance;
      Ranked(ActivityWithHoursDTO a,double distanceKm,double score,double relevance){this.a=a;this.distanceKm=distanceKm;this.score=score;this.relevance=relevance;}
    }

    List<Ranked> ranked = new ArrayList<>();
    for (ActivityWithHoursDTO a : activities) {
      if (a.getLatitude() == null || a.getLongitude() == null) continue;
      double d = haversineKm(lat, lon, a.getLatitude(), a.getLongitude());
      if (radiusKm != null && d > radiusKm) continue;
      if (!isOpen(a, now)) continue;
      // filter by context if provided (simple equality match)
      if (context != null && !"general".equalsIgnoreCase(context)) {
        if (a.getType() == null || !a.getType().trim().equalsIgnoreCase(context.trim())) continue;
      }

      double ds = distanceScore(d, radiusKm == null ? 1.0 : radiusKm);
      double rs = ratingScore(a.getRating());
      double ps = popularityScore(a.getUserRatingCount());
      double cr = categoryRelevance(a.getType(), context);
      double total = recommendationScore(ds, rs, ps, cr);
      ranked.add(new Ranked(a, d, total, cr));
    }

    ranked.sort(Comparator.comparingDouble((Ranked r) -> -r.score)
        .thenComparingDouble(r -> r.distanceKm)
        .thenComparing(r -> r.a.getName(), Comparator.nullsFirst(String::compareTo)));

    // Build list of RecommendedActivitiesDTO from ranked results
    List<RecommendedActivitiesDTO> recommended = new ArrayList<>();
    for (Ranked r : ranked) {
      boolean open = isOpen(r.a, now);
      // categoryRelevance stored as string (keep numeric formatted)
      String catRel = String.valueOf(Math.round(r.relevance * 1000.0) / 1000.0);
      RecommendedActivitiesDTO ra = new RecommendedActivitiesDTO(
          r.a.getId(),
          r.a.getName(),
          r.a.getType(),
          r.a.getLatitude(),
          r.a.getLongitude(),
          r.a.getRating(),
          r.a.getUserRatingCount(),
          Math.round(r.distanceKm * 10000.0) / 10000.0,
          r.score,
          catRel,
          open,
          now
      );
      recommended.add(ra);
    }

    RecommendationsDTO dto = new RecommendationsDTO();
    dto.setUserId(null);
    Map<String,Double> loc = new HashMap<>();
    loc.put("lat", lat);
    loc.put("lon", lon);
    dto.setUserLocation(loc);
    dto.setRadiusKm(radiusKm);
    dto.setContext(context);
    dto.setResponseTimestamp(now);
    dto.setResultCount(recommended.size());
    dto.setActivities(recommended);

    List<RecommendationsDTO> out = new ArrayList<>();
    out.add(dto);
    return out;
  }

}

