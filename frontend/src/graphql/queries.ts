import { gql } from '@apollo/client';

export const ME_QUERY = gql`
  query Me {
    me {
      id
      email
      name
      avatarUrl
      role
      team {
        id
        name
      }
    }
  }
`;

export const MY_TEAM_QUERY = gql`
  query MyTeam {
    myTeam {
      id
      name
      inviteCode
      members {
        id
        name
        avatarUrl
        role
      }
    }
  }
`;

export const DASHBOARD_QUERY = gql`
  query DashboardData($teamId: ID!, $date: String!) {
    team(id: $teamId) {
      name
      members {
        id
        name
        avatarUrl
      }
      stats {
        avgMood
        completionRate
        blockerCount
        totalStandups
      }
      moodTrend(days: 30) {
        date
        avgMood
        count
      }
      standups(date: $date) {
        id
        author {
          name
          avatarUrl
        }
        yesterday
        today
        blockers
        mood
        createdAt
      }
    }
  }
`;

export const MY_STANDUPS_QUERY = gql`
  query MyStandups($limit: Int, $offset: Int) {
    myStandups(limit: $limit, offset: $offset) {
      id
      yesterday
      today
      blockers
      mood
      createdAt
    }
  }
`;

export const TODAY_STANDUP_QUERY = gql`
  query TodayStandup {
    todayStandup {
      id
      yesterday
      today
      blockers
      mood
    }
  }
`;

export const TEAM_STATS_QUERY = gql`
  query TeamStats($teamId: ID!) {
    teamStats(teamId: $teamId) {
      avgMood
      completionRate
      blockerCount
      totalStandups
    }
  }
`;
