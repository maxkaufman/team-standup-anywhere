import { useState } from 'react';
import { useMutation, useQuery } from '@apollo/client/react';
import { useNavigate, Link } from 'react-router-dom';
import { SUBMIT_STANDUP_MUTATION } from '../graphql/mutations';
import { TODAY_STANDUP_QUERY } from '../graphql/queries';

const moodOptions = [
  { value: 1, emoji: '😞', label: 'Rough' },
  { value: 2, emoji: '😕', label: 'Meh' },
  { value: 3, emoji: '😐', label: 'Okay' },
  { value: 4, emoji: '🙂', label: 'Good' },
  { value: 5, emoji: '😄', label: 'Great' },
];

export default function SubmitStandup() {
  const [yesterday, setYesterday] = useState('');
  const [today, setToday] = useState('');
  const [blockers, setBlockers] = useState('');
  const [mood, setMood] = useState(3);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const { data: todayData, loading: checkingToday } = useQuery(TODAY_STANDUP_QUERY);

  const [submitMutation, { loading }] = useMutation(SUBMIT_STANDUP_MUTATION, {
    onCompleted: () => navigate('/dashboard'),
    onError: (err) => setError(err.message),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    submitMutation({
      variables: {
        input: { yesterday, today, blockers: blockers || null, mood },
      },
    });
  };

  if (checkingToday) return <div className="p-8 text-center text-gray-500">Loading...</div>;

  if (todayData?.todayStandup) {
    return (
      <div className="max-w-2xl mx-auto p-6 text-center">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Already submitted today</h1>
        <p className="text-gray-500 mb-6">You've already submitted your standup for today.</p>
        <Link
          to="/history"
          className="inline-block bg-indigo-600 text-white rounded-lg px-6 py-2.5 text-sm font-medium hover:bg-indigo-700"
        >
          View My History
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Daily Standup</h1>

      {error && (
        <div className="bg-red-50 text-red-700 rounded-lg p-3 mb-4 text-sm">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">What did you do yesterday?</label>
          <textarea
            value={yesterday}
            onChange={(e) => setYesterday(e.target.value)}
            rows={3}
            className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">What will you do today?</label>
          <textarea
            value={today}
            onChange={(e) => setToday(e.target.value)}
            rows={3}
            className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Any blockers?</label>
          <textarea
            value={blockers}
            onChange={(e) => setBlockers(e.target.value)}
            rows={2}
            className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            placeholder="Optional"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-3">How are you feeling?</label>
          <div className="flex justify-center gap-4">
            {moodOptions.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => setMood(option.value)}
                className={`flex flex-col items-center gap-1 p-3 rounded-xl transition ${
                  mood === option.value
                    ? 'bg-indigo-100 ring-2 ring-indigo-500'
                    : 'bg-gray-50 hover:bg-gray-100'
                }`}
              >
                <span className="text-2xl">{option.emoji}</span>
                <span className="text-xs text-gray-600">{option.label}</span>
              </button>
            ))}
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-indigo-600 text-white rounded-lg py-2.5 text-sm font-medium hover:bg-indigo-700 transition disabled:opacity-50"
        >
          {loading ? 'Submitting...' : 'Submit Standup'}
        </button>
      </form>
    </div>
  );
}
