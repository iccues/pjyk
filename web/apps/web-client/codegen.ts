import type { CodegenConfig } from "@graphql-codegen/cli";

const config: CodegenConfig = {
  overwrite: true,
  schema: "../../../meta-anime-backend/src/main/resources/graphql/schema.graphqls",
  documents: "src/**/*.graphql",
  generates: {
    "src/graphql/generated/": {
      preset: "client",
      presetConfig: {
        fragmentMasking: false,
      },
      config: {
        useTypeImports: true,
        enumsAsTypes: true,
        scalars: {
          ID: "number",
        },
      },
      plugins: [],
    },
  },
};

export default config;
