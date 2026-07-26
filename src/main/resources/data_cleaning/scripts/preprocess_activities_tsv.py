import os

import pandas as pd
import preprocess_working_hours as pwh
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent.parent

INPUT_FILE = BASE_DIR / "data" / "input" 
OUTPUT_FILE = BASE_DIR / "data" / "output" / "cleaned_activities.csv"


def preprocess_activities():
    # Step 0: Delete existing output file if it exists
    if OUTPUT_FILE.exists():
        OUTPUT_FILE.unlink()
        print(f"Deleted existing file: {OUTPUT_FILE}")

    # Step 1: Match TSV columns with backend Activity schema
    columns_mapping = {
        "places/displayName/text": "name",
        "places/internationalPhoneNumber": "phone_number",
        "places/location/latitude": "latitude",
        "places/location/longitude": "longitude",
        "places/rating": "rating",
        "places/userRatingCount": "user_rating_count",
        "places/primaryType": "type",
        "places/regularOpeningHours/weekdayDescriptions/0": "monday",
        "places/regularOpeningHours/weekdayDescriptions/1": "tuesday",
        "places/regularOpeningHours/weekdayDescriptions/2": "wednesday",
        "places/regularOpeningHours/weekdayDescriptions/3": "thursday",
        "places/regularOpeningHours/weekdayDescriptions/4": "friday",
        "places/regularOpeningHours/weekdayDescriptions/5": "saturday",
        "places/regularOpeningHours/weekdayDescriptions/6": "sunday",
    }

    processed_frames = []

    # Step 2-6: Process each TSV file in the input directory
    for filename in sorted(os.listdir(INPUT_FILE)):
        if filename.endswith(".tsv"):
            # Step 2: Read TSV datasets
            df = pd.read_csv(INPUT_FILE / filename, sep="\t")

            print("Original columns:")
            print(df.columns.tolist())

            # Step 3: Keep only useful backend fields
            df = df[list(columns_mapping.keys())]

            # Step 4: Rename columns
            df = df.rename(columns=columns_mapping)

            # Step 5: Normalize missing values
            df["phone_number"] = df["phone_number"].fillna("")
            df["rating"] = df["rating"].fillna(0)
            df["user_rating_count"] = df["user_rating_count"].fillna(0)
            df["type"] = df["type"].fillna("other")

            working_days = [
                "monday",
                "tuesday",
                "wednesday",
                "thursday",
                "friday",
                "saturday",
                "sunday",
            ]

            for day in working_days:
                df[day] = df[day].fillna("")

            processed_frames.append(df)

    if not processed_frames:
        raise FileNotFoundError(f"No TSV files found in {INPUT_FILE}")

    combined_df = pd.concat(processed_frames, ignore_index=True)
    combined_df.to_csv(OUTPUT_FILE, index=False)

    pwh.build_working_hours(OUTPUT_FILE)
    final_df = combined_df.drop(columns=working_days)
    final_df.to_csv(OUTPUT_FILE, index=False)
    print(f"\nFinal cleaned file with working hours created: {OUTPUT_FILE}")


if __name__ == "__main__":
    preprocess_activities()
