/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {defineConfig, transformWithEsbuild} from 'vite';
import react from '@vitejs/plugin-react';
import svgr from 'vite-plugin-svgr';
import {readdirSync} from 'fs';

export default defineConfig({
  base: '',
  plugins: [
    {
      name: 'treat-js-files-as-jsx',
      async transform(code, id) {
        if (!id.match(/src\/.*\.js$/)) return null;

        // Use the exposed transform from vite, instead of directly
        // transforming with esbuild
        return transformWithEsbuild(code, id, {
          loader: 'jsx',
          jsx: 'automatic',
        });
      },
    },
    react(),
    svgr({
      svgrOptions: {exportType: 'default', ref: true, svgo: false, titleProp: true},
      include: '**/*.svg',
    }),
  ],
  optimizeDeps: {
    force: true,
    esbuildOptions: {
      loader: {
        '.js': 'jsx',
      },
    },
  },
  resolve: {
    alias: generateAliases(),
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '^/(api|external/api|external/static)': {
        target: 'http://localhost:8090',
      },
      '^/': {
        target: 'http://localhost:8090',
        bypass: (req) => {
          const path = req.url;
          if (path?.includes('/sso-callback')) {
            return;
          }

          if (
            req.headers.cookie?.includes('X-Optimize-Authorization') ||
            req.headers.cookie?.includes('X-Optimize-Refresh-Token')
          ) {
            return path;
          }

          if (path === '/' || path?.includes('/sso/auth0')) {
            return;
          }
        },
      },
    },
  },
});

// Function to generate aliases dynamically
function generateAliases() {
  const aliases: Record<string, string> = {};

  readdirSync('src/modules').forEach((item) => {
    const aliasKey = item.replace(/\.[^/.]+$/, ''); // Remove file extension if present
    aliases[aliasKey] = `/src/modules/${aliasKey}`;
  });

  return aliases;
}
