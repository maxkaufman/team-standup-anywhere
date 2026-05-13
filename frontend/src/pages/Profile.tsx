import { useState } from 'react';
import { useMutation } from '@apollo/client/react';
import { useAuth } from '../hooks/useAuth';
import { UPDATE_PROFILE_MUTATION, GENERATE_AVATAR_URL_MUTATION } from '../graphql/mutations';
import { ME_QUERY } from '../graphql/queries';

export default function Profile() {
  const { user } = useAuth();
  const [name, setName] = useState(user?.name || '');
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState('');

  const [updateProfile, { loading }] = useMutation(UPDATE_PROFILE_MUTATION, {
    refetchQueries: [{ query: ME_QUERY }],
    onCompleted: () => setMessage('Profile updated!'),
    onError: (err) => setMessage(err.message),
  });

  const [generateUrl] = useMutation(GENERATE_AVATAR_URL_MUTATION);

  const handleAvatarUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);
    try {
      const { data } = await generateUrl({
        variables: { fileName: file.name, contentType: file.type },
      });

      // Upload directly to S3 via presigned URL
      await fetch(data.generateAvatarUploadUrl.uploadUrl, {
        method: 'PUT',
        body: file,
        headers: { 'Content-Type': file.type },
      });

      // Update profile with the S3 file URL
      await updateProfile({
        variables: { input: { avatarUrl: data.generateAvatarUploadUrl.fileUrl } },
      });

      setMessage('Avatar uploaded!');
    } catch (err) {
      setMessage('Upload failed');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto p-6">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Profile</h1>

      {message && (
        <div className="bg-indigo-50 text-indigo-700 rounded-lg p-3 mb-4 text-sm">{message}</div>
      )}

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        {/* Avatar */}
        <div className="flex flex-col items-center mb-6">
          <div className="w-20 h-20 rounded-full bg-indigo-100 flex items-center justify-center text-2xl font-bold text-indigo-700 mb-3">
            {user?.avatarUrl ? (
              <img src={user.avatarUrl} alt="Avatar" className="w-20 h-20 rounded-full object-cover" />
            ) : (
              user?.name.charAt(0).toUpperCase()
            )}
          </div>
          <label className="text-sm text-indigo-600 font-medium cursor-pointer hover:underline">
            {uploading ? 'Uploading...' : 'Change avatar'}
            <input type="file" accept="image/*" onChange={handleAvatarUpload} className="hidden" />
          </label>
        </div>

        {/* Name */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            updateProfile({ variables: { input: { name } } });
          }}
          className="space-y-4"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              value={user?.email || ''}
              disabled
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-4 py-2.5 text-sm text-gray-500"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-indigo-600 text-white rounded-lg py-2.5 text-sm font-medium hover:bg-indigo-700 transition disabled:opacity-50"
          >
            {loading ? 'Saving...' : 'Save Changes'}
          </button>
        </form>
      </div>
    </div>
  );
}
