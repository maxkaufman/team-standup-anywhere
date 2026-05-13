import { useQuery } from '@apollo/client/react';
import { useAuth } from '../hooks/useAuth';
import { DASHBOARD_QUERY } from '../graphql/queries';
import StatsCard from '../components/StatsCard';
import MoodChart from '../components/MoodChart';
import StandupCard from '../components/StandupCard';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const { user } = useAuth();
  const teamId = user?.team?.id;

  const today = new Date().toISOString().split('T')[0];

  const { data, loading, error } = useQuery(DASHBOARD_QUERY, {
    variables: { teamId, date: today },
    skip: !teamId,
  });

  if (!teamId) {
    return (
      <div className="p-8 text-center">
        <h2 className="text-xl font-bold text-gray-800 mb-4">No Team Yet</h2>
        <p className="text-gray-500 mb-6">Create or join a team to see your dashboard.</p>
        <Link
          to="/team"
          className="inline-block bg-indigo-600 text-white rounded-lg px-6 py-2.5 text-sm font-medium hover:bg-indigo-700"
        >
          Set Up Team
        </Link>
      </div>
    );
  }

  if (loading) return <div className="p-8 text-center text-gray-500">Loading dashboard...</div>;
  if (error) return <div className="p-8 text-center text-red-500">Error: {error.message}</div>;

  const team = data?.team;

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">{team.name} Dashboard</h1>
        <Link
          to="/standup"
          className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-indigo-700"
        >
          Submit Standup
        </Link>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <StatsCard title="Avg Mood" value={team.stats.avgMood} subtitle="Last 7 days" />
        <StatsCard title="Completion Rate" value={`${team.stats.completionRate}%`} subtitle="Today" color="text-green-600" />
        <StatsCard title="Blockers" value={team.stats.blockerCount} subtitle="Today" color="text-red-600" />
        <StatsCard title="Total Standups" value={team.stats.totalStandups} subtitle="All time" color="text-gray-700" />
      </div>

      {/* Mood Trend Chart */}
      <MoodChart data={team.moodTrend} />

      {/* Today's Standups */}
      <div>
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Today's Standups</h2>
        <div className="space-y-4">
          {team.standups.length === 0 ? (
            <p className="text-gray-400 text-sm">No standups submitted yet today.</p>
          ) : (
            team.standups.map((standup: any) => (
              <StandupCard key={standup.id} {...standup} />
            ))
          )}
        </div>
      </div>

      {/* Team Members */}
      <div>
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Team Members</h2>
        <div className="flex flex-wrap gap-3">
          {team.members.map((member: any) => (
            <div key={member.id} className="flex items-center gap-2 bg-white rounded-full px-4 py-2 shadow-sm border border-gray-100">
              <div className="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center text-xs font-medium text-indigo-700">
                {member.name.charAt(0).toUpperCase()}
              </div>
              <span className="text-sm text-gray-700">{member.name}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
