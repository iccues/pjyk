import { cacheExchange, Client, fetchExchange } from "@urql/vue";

export const client = new Client({
  url: "/api/graphql",
  preferGetMethod: false,
  exchanges: [cacheExchange, fetchExchange],
});
