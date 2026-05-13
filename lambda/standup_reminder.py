"""
AWS Lambda: Standup Reminder

Triggered by EventBridge on a cron schedule (e.g., weekdays at 9am).
Queries the database for team members who haven't submitted a standup today,
then sends reminder emails via SES.

EventBridge Rule: cron(0 14 ? * MON-FRI *)  # 9am EST = 2pm UTC
"""

import json
import os
import boto3
import psycopg2
from datetime import datetime, timezone

ses_client = boto3.client("ses", region_name=os.environ.get("AWS_REGION", "us-east-1"))
SENDER_EMAIL = os.environ.get("SENDER_EMAIL", "noreply@teampulse.app")


def get_db_connection():
    return psycopg2.connect(
        host=os.environ["DB_HOST"],
        port=os.environ.get("DB_PORT", "5432"),
        dbname=os.environ["DB_NAME"],
        user=os.environ["DB_USERNAME"],
        password=os.environ["DB_PASSWORD"],
    )


def get_members_without_standup(conn):
    """Find team members who haven't submitted a standup today."""
    today = datetime.now(timezone.utc).strftime("%Y-%m-%d")

    query = """
        SELECT u.email, u.name, t.name as team_name
        FROM users u
        JOIN teams t ON u.team_id = t.id
        WHERE u.team_id IS NOT NULL
        AND u.id NOT IN (
            SELECT DISTINCT author_id
            FROM standups
            WHERE created_at::date = %s
        )
    """

    with conn.cursor() as cur:
        cur.execute(query, (today,))
        return cur.fetchall()


def send_reminder_email(email, name, team_name):
    """Send a standup reminder via SES."""
    subject = f"Reminder: Submit your daily standup for {team_name}"
    body_html = f"""
    <html>
    <body style="font-family: -apple-system, sans-serif; max-width: 600px; margin: 0 auto;">
        <div style="padding: 24px;">
            <h2 style="color: #4f46e5;">Hey {name}! 👋</h2>
            <p style="color: #374151; line-height: 1.6;">
                You haven't submitted your daily standup for <strong>{team_name}</strong> yet today.
            </p>
            <p style="color: #374151; line-height: 1.6;">
                Take a minute to share what you're working on — your team is counting on you!
            </p>
            <a href="https://teampulse.app/standup"
               style="display: inline-block; background: #4f46e5; color: white;
                      padding: 12px 24px; border-radius: 8px; text-decoration: none;
                      font-weight: 600; margin-top: 16px;">
                Submit Standup
            </a>
            <p style="color: #9ca3af; font-size: 12px; margin-top: 24px;">
                TeamPulse — Async standups for remote teams
            </p>
        </div>
    </body>
    </html>
    """

    ses_client.send_email(
        Source=SENDER_EMAIL,
        Destination={"ToAddresses": [email]},
        Message={
            "Subject": {"Data": subject},
            "Body": {"Html": {"Data": body_html}},
        },
    )


def handler(event, context):
    """Lambda entry point — triggered by EventBridge schedule."""
    print(f"Standup reminder triggered at {datetime.now(timezone.utc).isoformat()}")

    conn = get_db_connection()
    try:
        members = get_members_without_standup(conn)
        print(f"Found {len(members)} members without standups today")

        sent_count = 0
        for email, name, team_name in members:
            try:
                send_reminder_email(email, name, team_name)
                sent_count += 1
                print(f"Sent reminder to {email}")
            except Exception as e:
                print(f"Failed to send to {email}: {e}")

        return {
            "statusCode": 200,
            "body": json.dumps({
                "message": f"Sent {sent_count} reminders",
                "total_pending": len(members),
            }),
        }
    finally:
        conn.close()
