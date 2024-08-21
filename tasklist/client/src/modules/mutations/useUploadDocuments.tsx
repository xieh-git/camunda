/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {useMutation} from '@tanstack/react-query';
import {api} from 'modules/api';
import {RequestError, request} from 'modules/request';

type Payload = {
  files: File[];
};

function useUploadDocuments() {
  return useMutation<null, RequestError | Error, Payload>({
    mutationFn: async (payload) => {
      const {response, error} = await request(api.uploadDocuments(payload));

      if (response !== null) {
        return null;
      }

      throw error;
    },
  });
}

export {useUploadDocuments};
