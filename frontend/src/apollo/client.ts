import { ApolloClient, InMemoryCache, ApolloLink, createHttpLink } from '@apollo/client';

const httpLink = createHttpLink({
  uri: '/graphql',
});

// Middleware link that injects the JWT token on every request.
// Uses ApolloLink directly — Apollo Client 4 deprecated the setContext helper.
const authLink = new ApolloLink((operation, forward) => {
  const token = localStorage.getItem('token');
  operation.setContext(({ headers = {} }) => ({
    headers: {
      ...headers,
      authorization: token ? `Bearer ${token}` : '',
    },
  }));
  return forward(operation);
});

export const client = new ApolloClient({
  link: authLink.concat(httpLink),
  cache: new InMemoryCache(),
});
