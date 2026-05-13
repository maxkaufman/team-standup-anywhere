import { useState } from 'react';
import { useMutation, useQuery } from '@apollo/client/react';
import { MY_TEAM_QUERY, ME_QUERY } from '../graphql/queries';
import { CREATE_TEAM_MUTATION, JOIN_TEAM_MUTATION } from '../graphql/mutations';

export default function TeamManagement() {
  const [teamName, setTeamName] = useState('');
  const [inviteCode, setInviteCode] = useState('');
  const [error, setError] = useState('');

  const { data, loading, refetch } = useQuery(MY_TEAM_QUERY);

  const [createTeam, { loading: creating }] = useMutation(CREATE_TEAM_MUTATION, {
    refetchQueries: [{ query: ME_QUERY }],
    onCompleted: () => refetch(),
    onError: (err) => setError(err.message),
  });

  const [joinTeam, { loading: joining }] = useMutation(JOIN_TEAM_MUTATION, {
    refetchQueries: [{ query: ME_QUERY }],
    onCompleted: () => refetch(),
    onError: (err) => setError(err.message),
  });

  if (loading) return <div className="p-8 text-center text-gray-500">Loading...</div>;

  const team = data?.myTeam;

  if (team) {
    return (
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Team: {team.name}</h1>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
          <p className="text-sm text-gray-500 mb-1">Invite Code</p>
          <p className="text-lg font-mono font-bold text-indigo-600">{team.inviteCode}</p>
          <p className="text-xs text-gray-400 mt-1">Share this code with teammates to join</p>
        </div>

        <h2 className="text-lg font-semibold text-gray-800 mb-3">Members ({team.members.length})</h2>
        <div className="space-y-2">
          {team.members.map((member: any) => (
            <div key={member.id} className="flex items-center justify-between bg-white rounded-lg border border-gray-100 px-4 py-3">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-sm font-medium text-indigo-700">
                  {member.name.charAt(0).toUpperCase()}
                </div>
                <span className="text-sm font-medium text-gray-700">{member.name}</span>
              </div>
              <span className="text-xs text-gray-400 uppercase">{member.role}</span>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-md mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6 text-center">Set Up Your Team</h1>

      {error && (
        <div className="bg-red-50 text-red-700 rounded-lg p-3 mb-4 text-sm">{error}</div>
      )}

      {/* Create Team */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Create a Team</h2>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            createTeam({ variables: { name: teamName } });
          }}
          className="flex gap-2"
        >
          <input
            value={teamName}
            onChange={(e) => setTeamName(e.target.value)}
            placeholder="Team name"
            className="flex-1 rounded-lg border border-gray-300 px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
          <button
            type="submit"
            disabled={creating}
            className="bg-indigo-600 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
          >
            Create
          </button>
        </form>
      </div>

      <div className="text-center text-gray-400 text-sm mb-6">or</div>

      {/* Join Team */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Join a Team</h2>
        <form
          onSubmit={(e) => {
            e.preventDefault();
            joinTeam({ variables: { inviteCode } });
          }}
          className="flex gap-2"
        >
          <input
            value={inviteCode}
            onChange={(e) => setInviteCode(e.target.value)}
            placeholder="Invite code"
            className="flex-1 rounded-lg border border-gray-300 px-4 py-2 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500"
            required
          />
          <button
            type="submit"
            disabled={joining}
            className="bg-gray-800 text-white rounded-lg px-4 py-2 text-sm font-medium hover:bg-gray-900 disabled:opacity-50"
          >
            Join
          </button>
        </form>
      </div>
    </div>
  );
}
