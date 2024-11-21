import react from 'eslint-plugin-react';
import babelParser from '@babel/eslint-parser';

export default [
  {
    files: ['**/*.{js,jsx,ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2021,
      sourceType: 'module',
      parser: babelParser, // Use babel parser here
      parserOptions: {
        requireConfigFile: false, // Allows Babel to work without a babel.config.js
        babelOptions: {
          presets: ['@babel/preset-react'], // Ensures JSX syntax is understood
        },
      },
    },
    plugins: {
      react,
    },
    rules: {
      'react/prop-types': 'off', // Disable PropTypes validation
    },
    settings: {
      react: {
        version: 'detect', // Automatically detect React version
      },
    },
    ignores: [
      'build/',      // Ignore the build folder
      'node_modules/' // Ignore node_modules
    ],
  },
];