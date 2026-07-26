import pandas as pd
import re
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

# INPUT_FILE = BASE_DIR / "data" / "cleaned_activities.csv"
OUTPUT_FILE = BASE_DIR / "data" / "output" / "working_hours.csv"

DAYS = ["monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"]


def parse_time(time_str: str) -> str:
    """Convert '8:00 AM' -> '08:00', '10:00 PM' -> '22:00'"""
    time_str = time_str.strip()
    match = re.match(r"(\d+):(\d+)\s*(AM|PM)", time_str, re.IGNORECASE)
    if not match:
        return None
    hour, minute, period = int(match[1]), int(match[2]), match[3].upper()
    if period == "PM" and hour != 12:
        hour += 12
    elif period == "AM" and hour == 12:
        hour = 0
    return f"{hour:02d}:{minute:02d}"


def parse_day_string(raw: str):
    """
    Returns a list of open intervals for a single day.
    Handles: normal hours, Open 24 hours, Closed, multiple intervals, missing.
    """
    if not raw or not isinstance(raw, str) or raw.strip() == "":
        return []  # Missing — skip row entirely

    # Strip the leading "Monday: " prefix
    _, _, value = raw.partition(":")
    value = value.strip()

    if re.search(r"closed", value, re.IGNORECASE):
        return [
            {
                "open_time": None,
                "close_time": None,
                "is_open_24h": False,
                "is_closed": True,
            }
        ]

    if re.search(r"open 24 hours", value, re.IGNORECASE):
        return [
            {
                "open_time": "00:00",
                "close_time": "23:59",
                "is_open_24h": True,
                "is_closed": False,
            }
        ]

    # Split on comma for multiple intervals: "8:00 AM – 12:00 PM, 2:00 PM – 6:00 PM"
    intervals = value.split(",")
    results = []
    for interval in intervals:
        parts = re.split(r"[–\-]", interval)  # en-dash or hyphen
        if len(parts) == 2:
            open_time = parse_time(parts[0])
            close_time = parse_time(parts[1])
            if open_time and close_time:
                results.append(
                    {
                        "open_time": open_time,
                        "close_time": close_time,
                        "is_open_24h": False,
                        "is_closed": False,
                    }
                )
    return results


def build_day_entry(day: str, intervals: list[dict]) -> dict:
    if not intervals:
        return {
            "day_of_week": day,
            "open_time": None,
            "close_time": None,
            "break_hour_start": None,
            "break_hour_end": None,
            "is_open_24h": False,
            "is_closed": True,
        }

    if len(intervals) == 1:
        interval = intervals[0]
        return {
            "day_of_week": day,
            "open_time": interval["open_time"],
            "close_time": interval["close_time"],
            "break_hour_start": None,
            "break_hour_end": None,
            "is_open_24h": interval["is_open_24h"],
            "is_closed": interval["is_closed"],
        }

    open_time = intervals[0]["open_time"]
    close_time = intervals[-1]["close_time"]

    break_hour_start = None
    break_hour_end = None
    for previous_interval, next_interval in zip(intervals, intervals[1:]):
        if previous_interval["close_time"] and next_interval["open_time"]:
            break_hour_start = previous_interval["close_time"]
            break_hour_end = next_interval["open_time"]
            break

    return {
        "day_of_week": day,
        "open_time": open_time,
        "close_time": close_time,
        "break_hour_start": break_hour_start,
        "break_hour_end": break_hour_end,
        "is_open_24h": False,
        "is_closed": False,
    }


def build_working_hours(INPUT_FILE):
    df = pd.read_csv(INPUT_FILE)

    rows = []
    for activity_id, row in enumerate(df.itertuples(), start=1):
        for day in DAYS:
            raw = getattr(row, day, "")
            parsed = parse_day_string(raw)
            rows.append(
                {
                    "activity_id": activity_id,
                    **build_day_entry(day, parsed),
                }
            )

    hours_df = pd.DataFrame(
        rows,
        columns=[
            "activity_id",
            "day_of_week",
            "open_time",
            "close_time",
            "break_hour_start",
            "break_hour_end",
            "is_open_24h",
            "is_closed",
        ],
    )
    hours_df.to_csv(OUTPUT_FILE, index=False)

    print(f"Working hours rows: {len(hours_df)}")
    print(f"Output: {OUTPUT_FILE}")
    print(hours_df.head(10))


if __name__ == "__main__":
    build_working_hours()
