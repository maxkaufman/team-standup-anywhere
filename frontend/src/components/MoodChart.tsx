import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface MoodDataPoint {
  date: string;
  avgMood: number;
  count: number;
}

export default function MoodChart({ data }: { data: MoodDataPoint[] }) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">Mood Trend (30 days)</h3>
      <ResponsiveContainer width="100%" height={300}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis
            dataKey="date"
            tick={{ fontSize: 12 }}
            tickFormatter={(val) => new Date(val).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}
          />
          <YAxis domain={[1, 5]} tick={{ fontSize: 12 }} />
          <Tooltip
            labelFormatter={(val) => new Date(val).toLocaleDateString()}
            formatter={(value: number) => [value.toFixed(1), 'Avg Mood']}
          />
          <Line
            type="monotone"
            dataKey="avgMood"
            stroke="#6366f1"
            strokeWidth={2}
            dot={{ r: 4 }}
            activeDot={{ r: 6 }}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
