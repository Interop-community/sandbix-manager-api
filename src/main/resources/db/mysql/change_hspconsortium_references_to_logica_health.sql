UPDATE sandman.app
SET launch_uri = REPLACE(launch_uri, 'hspconsortium', 'logicahealth')
WHERE launch_uri like '%hspconsortium%';

UPDATE sandman.app
SET logo_uri = REPLACE(logo_uri, 'hspconsortium', 'logicahealth')
WHERE logo_uri like '%hspconsortium%';

UPDATE sandman.app
SET manifest_url = REPLACE(manifest_url, 'hspconsortium', 'logicahealth')
WHERE manifest_url like '%hspconsortium%';

UPDATE sandman.cds_hook
SET logo_uri = REPLACE(logo_uri, 'hspconsortium', 'logicahealth')
WHERE logo_uri like '%hspconsortium%';

UPDATE sandman.cds_hook
SET hook_url = REPLACE(hook_url, 'hspconsortium', 'logicahealth')
WHERE hook_url like '%hspconsortium%';

UPDATE sandman.cds_service_endpoint
SET url = REPLACE(url, 'hspconsortium', 'logicahealth')
WHERE url like '%hspconsortium%';

UPDATE sandman.config
SET value = REPLACE(value, 'hspconsortium', 'logicahealth')
WHERE value like '%hspconsortium%';

UPDATE sandman.sandbox_import
SET import_fhir_url = REPLACE(import_fhir_url, 'hspconsortium', 'logicahealth')
WHERE import_fhir_url like '%hspconsortium%';

UPDATE oic.authentication_holder_extension
SET val = REPLACE(val, 'hspconsortium', 'logicahealth')
WHERE val like '%hspconsortium%';

UPDATE oic.authentication_holder_request_parameter
SET val = REPLACE(val, 'hspconsortium', 'logicahealth')
WHERE val like '%hspconsortium%';

UPDATE oic.client_details
SET logo_uri = REPLACE(logo_uri, 'hspconsortium', 'logicahealth')
WHERE logo_uri like '%hspconsortium%';

UPDATE oic.client_details
SET jwks_uri = REPLACE(jwks_uri, 'hspconsortium', 'logicahealth')
WHERE jwks_uri like '%hspconsortium%';

UPDATE oic.client_redirect_uri
SET redirect_uri = REPLACE(redirect_uri, 'hspconsortium', 'logicahealth')
WHERE redirect_uri like '%hspconsortium%';
