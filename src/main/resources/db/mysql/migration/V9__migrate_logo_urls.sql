# migrate logo urls

UPDATE app
SET logo_uri=REPLACE(logo_uri, 'sandbox.hspconsortium.org/REST', 'sandbox-api.hspconsortium.org')
WHERE logo_uri LIKE '%REST%';
