import { useQuery } from '@apollo/client/react';
import { MY_STANDUPS_QUERY } from '../graphql/queries';

const moodEmoji = ['', '😞', '😕', '😐', '🙂', '😄'];

export default function StandupHistory() {
  const { data, loading, error } = useQuery(MY_STANDUPS_QUERY, {
    variables: { limit: 30, offset: 0 },
  });

  if (loading) return <div className="p-8 text-center text-gray-500">Loading...</div>;
  if (error) return <div className="p-8 text-center text-red-500">Error: {error.message}</div>;

  const standups = data?.myStandups || [];

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Standup History</h1>

      {standups.length === 0 ? (
        <p className="text-gray-400 text-center">No standups yet. Submit your first one!</p>
      ) : (
        <div className="space-y-4">
          {standups.map((standup: any) => (
            <div key={standup.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm text-gray-500">
                  {new Date(standup.createdAt).toLocaleDateString('en-US', {
                    weekday: 'long',
                    month: 'short',
                    day: 'numeric',
                  })}
                </span>
                <span className="text-xl">{moodEmoji[standup.mood]}</span>
              </div>
              <div className="space-y-2 text-sm">
                <div>
                  <span className="font-medium text-gray-600">Yesterday: </span>
                  <span className="text-gray-700">{standup.yesterday}</span>
                </div>
                <div>
                  <span className="font-medium text-gray-600">Today: </span>
                  <span className="text-gray-700">{standup.today}</span>
                </div>
                {standup.blockers && (
                  <div className="bg-red-50 rounded-lg p-2">
                    <span className="font-medium text-red-600">Blockers: </span>
                    <span className="text-red-700">{standup.blockers}</span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
