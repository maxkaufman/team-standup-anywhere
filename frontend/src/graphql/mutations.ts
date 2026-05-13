import { gql } from '@apollo/client';

export const SIGN_UP_MUTATION = gql`
  mutation SignUp($input: SignUpInput!) {
    signUp(input: $input) {
      token
      refreshToken
      user {
        id
        email
        name
      }
    }
  }
`;

export const LOGIN_MUTATION = gql`
  mutation Login($email: String!, $password: String!) {
    login(email: $email, password: $password) {
      token
      refreshToken
      user {
        id
        email
        name
      }
    }
  }
`;

export const SUBMIT_STANDUP_MUTATION = gql`
  mutation SubmitStandup($input: StandupInput!) {
    submitStandup(input: $input) {
      id
      yesterday
      today
      blockers
      mood
      createdAt
    }
  }
`;

export const CREATE_TEAM_MUTATION = gql`
  mutation CreateTeam($name: String!) {
    createTeam(name: $name) {
      id
      name
      inviteCode
    }
  }
`;

export const JOIN_TEAM_MUTATION = gql`
  mutation JoinTeam($inviteCode: String!) {
    joinTeam(inviteCode: $inviteCode) {
      id
      name
    }
  }
`;

export const UPDATE_PROFILE_MUTATION = gql`
  mutation UpdateProfile($input: ProfileInput!) {
    updateProfile(input: $input) {
      id
      name
      avatarUrl
    }
  }
`;

export const GENERATE_AVATAR_URL_MUTATION = gql`
  mutation GenerateAvatarUploadUrl($fileName: String!, $contentType: String!) {
    generateAvatarUploadUrl(fileName: $fileName, contentType: $contentType) {
      uploadUrl
      fileUrl
    }
  }
`;
