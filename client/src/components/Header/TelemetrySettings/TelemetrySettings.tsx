/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a proprietary license.
 * See the License.txt file for more information. You may not use this file
 * except in compliance with the proprietary license.
 */

import {useState, useEffect} from 'react';
import {Button, Checkbox, Stack} from '@carbon/react';

import {Modal, DocsLink} from 'components';
import {t} from 'translation';
import {withErrorHandling, WithErrorHandlingProps} from 'HOC';
import {showError, addNotification} from 'notifications';
import {isMetadataTelemetryEnabled, loadConfig} from 'config';
import {updateTelemetry} from './service';

interface TelemetrySettingsProps extends WithErrorHandlingProps {
  onClose: () => void;
}

export function TelemetrySettings({onClose, mightFail}: TelemetrySettingsProps): JSX.Element {
  const [telemetryEnabled, setTelemetryEnabled] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  // set initial state of the checkbox
  useEffect(() => {
    (async () => {
      setTelemetryEnabled(await isMetadataTelemetryEnabled());
    })();
  }, []);

  function submit() {
    setIsLoading(true);
    mightFail(
      updateTelemetry(telemetryEnabled),
      () => {
        addNotification({type: 'success', text: t('telemetry.updated')});

        // ui-configuration has changed, we need to reload the config
        loadConfig();

        onClose();
      },
      (err) => {
        showError(err);
      },
      () => setIsLoading(false)
    );
  }

  return (
    <Modal className="TelemetrySettings" open onClose={onClose}>
      <Modal.Header title={t('telemetry.header')} />
      <Modal.Content>
        <Stack gap={4} orientation="vertical">
          <p>{t('telemetry.text')}</p>
          <Checkbox
            id="enableTelemetryCheckbox"
            labelText={
              <>
                <b>{t('telemetry.enable')}</b>
                <p>{t('telemetry.info')}</p>
              </>
            }
            checked={telemetryEnabled}
            onChange={(evt) => setTelemetryEnabled(evt.target.checked)}
          />
          <p>
            <b>{t('telemetry.respectPrivacy')} </b>
            {t('telemetry.personalData')}{' '}
            <DocsLink location="self-managed/optimize-deployment/configuration/telemetry/">
              {t('common.documentation')}
            </DocsLink>{' '}
            {t('telemetry.orView', {
              policy: t('telemetry.privacyPolicy'),
              link: 'https://camunda.com/legal/privacy/',
            })}
          </p>
        </Stack>
      </Modal.Content>
      <Modal.Footer>
        <Button kind="secondary" className="cancel" onClick={onClose} disabled={isLoading}>
          {t('common.cancel')}
        </Button>
        <Button className="confirm" disabled={isLoading} onClick={submit}>
          {t('common.save')}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}

export default withErrorHandling(TelemetrySettings);
