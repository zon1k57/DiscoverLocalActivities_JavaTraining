#!/usr/bin/env python3
"""
Fetch "activity" places (restaurants, cafes, bars, museums, attractions, etc.)
across all of North Macedonia from OpenStreetMap using the Overpass API,
and write them to a TSV file shaped similarly to a Google Places export.

No API key, no billing account, no rate-limit signup needed.

Usage:
    python3 fetch_mk_activities.py
    python3 fetch_mk_activities.py --output my_activities.tsv
    python3 fetch_mk_activities.py --categories restaurant,cafe,bar,museum

Notes:
- Uses the public Overpass API instance (overpass-api.de) by default.
  If it's slow/overloaded, swap OVERPASS_URL for a mirror (see comments below).
- Overpass free instances rate-limit and can time out on big areas -- this
  script queries by category to keep each request small, and sleeps
  between requests to be a good citizen.
"""

import argparse
import json
import sys
import time
import urllib.request
import urllib.parse
import urllib.error
import csv

# Public Overpass API endpoints. If one is overloaded, try another mirror:
#   https://overpass-api.de/api/interpreter        (main instance)
#   https://overpass.kumi.systems/api/interpreter   (mirror)
#   https://overpass.openstreetmap.ru/api/interpreter (mirror)
OVERPASS_URL = "https://overpass-api.de/api/interpreter"

# OSM tag => category label, mirroring "primaryType" from your Google export.
# Each entry is (osm_key, osm_value). Extend this freely -- OSM tag reference:
# https://wiki.openstreetmap.org/wiki/Map_features
DEFAULT_CATEGORIES = {
    "restaurant": ("amenity", "restaurant"),
    "cafe": ("amenity", "cafe"),
    "bar": ("amenity", "bar"),
    "pub": ("amenity", "pub"),
    "fast_food": ("amenity", "fast_food"),
    "bakery": ("shop", "bakery"),
    "museum": ("tourism", "museum"),
    "attraction": ("tourism", "attraction"),
    "viewpoint": ("tourism", "viewpoint"),
    "hotel": ("tourism", "hotel"),
    "guest_house": ("tourism", "guest_house"),
    "park": ("leisure", "park"),
    "nightclub": ("amenity", "nightclub"),
    "cinema": ("amenity", "cinema"),
    "theatre": ("amenity", "theatre"),
    "gallery": ("tourism", "gallery"),
    "playground": ("leisure", "playground"),
}

FIELDNAMES = [
    "places/id",
    "places/internationalPhoneNumber",
    "places/location/latitude",
    "places/location/longitude",
    "places/rating",
    "places/regularOpeningHours/weekdayDescriptions/0",
    "places/regularOpeningHours/weekdayDescriptions/1",
    "places/regularOpeningHours/weekdayDescriptions/2",
    "places/regularOpeningHours/weekdayDescriptions/3",
    "places/regularOpeningHours/weekdayDescriptions/4",
    "places/regularOpeningHours/weekdayDescriptions/5",
    "places/regularOpeningHours/weekdayDescriptions/6",
    "places/userRatingCount",
    "places/displayName/text",
    "places/displayName/languageCode",
    "places/primaryType",
    "places/priceLevel",
]

WEEKDAYS = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"]
WEEKDAY_FULL = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]


def build_query(osm_key: str, osm_value: str, element_type: str) -> str:
    """
    Build an Overpass QL query for one category and ONE element type
    (node or way) across all of North Macedonia.

    Splitting node/way into separate requests (instead of one combined
    query) keeps each request smaller, which matters a lot for avoiding
    504 Gateway Timeout on the free public instance for a country-wide
    query -- node queries in particular are much cheaper than way queries.
    """
    return f"""
[out:json][timeout:180];
area["ISO3166-1"="MK"][admin_level=2]->.mk;
(
  {element_type}["{osm_key}"="{osm_value}"](area.mk);
);
out center tags;
"""


def fetch_once(query: str) -> list:
    data = urllib.parse.urlencode({"data": query}).encode("utf-8")
    req = urllib.request.Request(
        OVERPASS_URL,
        data=data,
        method="POST",
        headers={
            # overpass-api.de returns HTTP 406 for the default urllib
            # User-Agent -- it filters generic bot-looking requests.
            # Any real identifying UA string works.
            "User-Agent": "mk-activities-fetcher/1.0 (student project)",
            "Content-Type": "application/x-www-form-urlencoded",
            "Accept": "*/*",
        },
    )
    # 180s socket timeout to match the [timeout:180] set server-side above --
    # the client timeout must be >= the server-side timeout or you'll get a
    # local timeout before the server even gets a chance to respond.
    with urllib.request.urlopen(req, timeout=200) as resp:
        payload = json.loads(resp.read().decode("utf-8"))
    return payload.get("elements", [])


def fetch_category(osm_key: str, osm_value: str, max_retries: int = 3) -> list:
    """
    Fetch a category as two smaller requests (nodes, then ways) rather than
    one combined request, with retries + backoff on 504/503 -- these are
    common and often transient on the free public Overpass instance,
    especially for country-wide queries.
    """
    all_elements = []
    for element_type in ("node", "way"):
        query = build_query(osm_key, osm_value, element_type)
        last_error = None
        for attempt in range(1, max_retries + 1):
            try:
                elements = fetch_once(query)
                all_elements.extend(elements)
                last_error = None
                break
            except urllib.error.HTTPError as e:
                last_error = e
                if e.code in (503, 504) and attempt < max_retries:
                    wait = 10 * attempt
                    print(
                        f"    {element_type} query got HTTP {e.code}, "
                        f"retrying in {wait}s (attempt {attempt}/{max_retries})...",
                        file=sys.stderr,
                    )
                    time.sleep(wait)
                    continue
                raise
        if last_error is not None:
            raise last_error
        time.sleep(1.5)  # small pause between the node and way sub-requests
    return all_elements


def parse_opening_hours(raw: str) -> list:
    """
    Very rough OSM opening_hours -> 7-day description list.
    OSM's opening_hours syntax is a mini-language; this handles the common
    cases (e.g. "Mo-Fr 09:00-18:00; Sa 09:00-14:00; Su off") but is not a
    full parser. Falls back to putting the raw string in every slot if it
    can't confidently split it.
    """
    result = [""] * 7
    if not raw:
        return result
    try:
        segments = [s.strip() for s in raw.split(";") if s.strip()]
        for seg in segments:
            parts = seg.split(" ", 1)
            if len(parts) != 2:
                continue
            day_part, time_part = parts
            days_covered = []
            for chunk in day_part.split(","):
                chunk = chunk.strip()
                if "-" in chunk and chunk not in WEEKDAYS:
                    start, end = chunk.split("-")
                    if start in WEEKDAYS and end in WEEKDAYS:
                        si, ei = WEEKDAYS.index(start), WEEKDAYS.index(end)
                        days_covered.extend(range(si, ei + 1) if si <= ei else list(range(si, 7)) + list(range(0, ei + 1)))
                elif chunk in WEEKDAYS:
                    days_covered.append(WEEKDAYS.index(chunk))
            desc = "Closed" if time_part.strip().lower() == "off" else time_part.strip()
            for di in days_covered:
                result[di] = f"{WEEKDAY_FULL[di]}: {desc}"
    except Exception:
        # Parsing failed -- don't lose the raw data, just leave it unparsed.
        return [raw] * 7
    # Fill any days that got missed with empty (not "Closed" -- we don't know)
    for i, v in enumerate(result):
        if not v:
            result[i] = ""
    return result


def element_to_row(el: dict, category_label: str) -> dict:
    tags = el.get("tags", {})
    if el["type"] == "node":
        lat, lon = el.get("lat"), el.get("lon")
    else:
        center = el.get("center", {})
        lat, lon = center.get("lat"), center.get("lon")

    name = tags.get("name", tags.get("name:en", ""))
    phone = tags.get("phone", tags.get("contact:phone", ""))
    opening_raw = tags.get("opening_hours", "")
    hours = parse_opening_hours(opening_raw)

    row = {
        "places/id": f"osm_{el['type']}_{el['id']}",
        "places/internationalPhoneNumber": phone,
        "places/location/latitude": lat,
        "places/location/longitude": lon,
        "places/rating": "",  # OSM has no built-in rating system
        "places/regularOpeningHours/weekdayDescriptions/0": hours[0],
        "places/regularOpeningHours/weekdayDescriptions/1": hours[1],
        "places/regularOpeningHours/weekdayDescriptions/2": hours[2],
        "places/regularOpeningHours/weekdayDescriptions/3": hours[3],
        "places/regularOpeningHours/weekdayDescriptions/4": hours[4],
        "places/regularOpeningHours/weekdayDescriptions/5": hours[5],
        "places/regularOpeningHours/weekdayDescriptions/6": hours[6],
        "places/userRatingCount": "",  # not available in OSM
        "places/displayName/text": name,
        "places/displayName/languageCode": "en" if tags.get("name:en") else "",
        "places/primaryType": category_label,
        "places/priceLevel": "",  # not consistently tagged in OSM
    }
    return row


def main():
    parser = argparse.ArgumentParser(description="Fetch NM activities from OpenStreetMap/Overpass")
    parser.add_argument("--output", default="mk_activities.tsv", help="Output TSV path")
    parser.add_argument(
        "--categories",
        default=",".join(DEFAULT_CATEGORIES.keys()),
        help="Comma-separated category labels to fetch (see DEFAULT_CATEGORIES in script)",
    )
    parser.add_argument("--sleep", type=float, default=2.0, help="Seconds to sleep between requests")
    args = parser.parse_args()

    wanted = [c.strip() for c in args.categories.split(",") if c.strip()]
    unknown = [c for c in wanted if c not in DEFAULT_CATEGORIES]
    if unknown:
        print(f"Unknown categories: {unknown}. Available: {list(DEFAULT_CATEGORIES.keys())}", file=sys.stderr)
        sys.exit(1)

    seen_ids = set()
    rows = []

    for label in wanted:
        osm_key, osm_value = DEFAULT_CATEGORIES[label]
        print(f"Fetching category '{label}' ({osm_key}={osm_value})...", file=sys.stderr)
        try:
            elements = fetch_category(osm_key, osm_value,10)
        except Exception as e:
            print(f"  FAILED for {label}: {e}", file=sys.stderr)
            continue

        added = 0
        for el in elements:
            uid = f"{el['type']}_{el['id']}"
            if uid in seen_ids:
                continue
            if not el.get("tags", {}).get("name"):
                continue  # skip unnamed places, not useful as "activities"
            seen_ids.add(uid)
            rows.append(element_to_row(el, label))
            added += 1
        print(f"  -> {added} new places (total so far: {len(rows)})", file=sys.stderr)

        time.sleep(args.sleep)  # be polite to the free public instance

    with open(args.output, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=FIELDNAMES, delimiter="\t")
        writer.writeheader()
        writer.writerows(rows)

    print(f"\nDone. Wrote {len(rows)} places to {args.output}", file=sys.stderr)


if __name__ == "__main__":
    main()