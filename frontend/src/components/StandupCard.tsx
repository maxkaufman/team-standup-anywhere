interface StandupCardProps {
  author: { name: string; avatarUrl?: string };
  yesterday: string;
  today: string;
  blockers?: string | null;
  mood: number;
  createdAt: string;
}

const moodEmoji = ['', '😞', '😕', '😐', '🙂', '😄'];

export default function StandupCard({ author, yesterday, today, blockers, mood, createdAt }: StandupCardProps) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-indigo-100 flex items-center justify-center text-sm font-medium text-indigo-700">
            {author.avatarUrl ? (
              <img src={author.avatarUrl} alt={author.name} className="w-9 h-9 rounded-full object-cover" />
            ) : (
              author.name.charAt(0).toUpperCase()
            )}
          </div>
          <span className="font-medium text-gray-800">{author.name}</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xl">{moodEmoji[mood]}</span>
          <span className="text-xs text-gray-400">
            {new Date(createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </span>
        </div>
      </div>

      <div className="space-y-2 text-sm">
        <div>
          <span className="font-medium text-gray-600">Yesterday: </span>
          <span className="text-gray-700">{yesterday}</span>
        </div>
        <div>
          <span className="font-medium text-gray-600">Today: </span>
          <span className="text-gray-700">{today}</span>
        </div>
        {blockers && (
          <div className="bg-red-50 rounded-lg p-2">
            <span className="font-medium text-red-600">Blockers: </span>
            <span className="text-red-700">{blockers}</span>
          </div>
        )}
      </div>
    </div>
  );
}
