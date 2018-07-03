INSERT INTO smart_app
(id, name, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author)
VALUES
  (
    'hspc-bilirubin-risk-chart',
    'Bilirubin Risk Chart',
    'https://bilirubin-risk-chart.hspconsortium.org/.well-known/smart/manifest.json',
    'bilirubin_chart',
    (SELECT id
     FROM user
     WHERE sbm_user_id = 'admin'),
    NOW(),
    'PUBLIC',
    'Patient?_id=BILIBABY,SMART-1288992',
    'https://IntermountainHealthcare.org',
    'The HSPC Bilirubin Risk Chart is a sample app that demonstrates many of the features of the SMART on FHIR app launch specification and HL7 FHIR standard.',
    'Intermountain Healthcare'
  ),
  (
    'hspc-patient-data-manager',
    'Patient Data Manager',
    'https://patient-data-manager.hspconsortium.org/.well-known/smart/manifest.json',
    'patient_data_manager',
    (SELECT id
     FROM user
     WHERE sbm_user_id = 'admin'),
    NOW(),
    'PUBLIC',
    '',
    'https://bitbucket.org/hspconsortium/patient-data-manager/src',
    'The HSPC Patient Data Manager app is a SMART on FHIR application that is used for managing the data of a single patient.',
    'HSPC'
  ),
  (
    'hspc-my-web-app',
    'My Web App',
    'https://content.hspconsortium.org/apps/my-web-app/.well-known/smart/manifest.json',
    'my_web_app',
    (SELECT id
     FROM user
     WHERE sbm_user_id = 'admin'),
    NOW(),
    'PUBLIC',
    '',
    'https://healthservices.atlassian.net/wiki/spaces/HSPC/pages/64159752/For+Developers',
    'Perform a SMART launch at http://localhost:8000/fhir-app/launch.html using the client: my_web_app.',
    'HSPC'
  ),
  (
    'cds-hooks-sandbox',
    'CDS Hooks Sandbox',
    'https://content.hspconsortium.org/apps/cds-hooks-sandbox/.well-known/smart/manifest.json',
    '48163c5e-88b5-4cb3-92d3-23b800caa927',
    (SELECT id
     FROM user
     WHERE sbm_user_id = 'admin'),
    NOW(),
    'PUBLIC',
    '',
    'http://cds-hooks.org/',
    'The CDS Hooks Sandbox is a tool that allows users to simulate the workflow of the CDS Hooks standard.',
    'CDS Hooks'
  );

CREATE TABLE sandbox_smart_apps (
  sandbox    INT(11) NOT NULL,
  smart_app  VARCHAR(36) NOT NULL,
  UNIQUE KEY (smart_app),
  KEY (sandbox),
  CONSTRAINT FOREIGN KEY (sandbox) REFERENCES sandbox (id),
  CONSTRAINT FOREIGN KEY (smart_app) REFERENCES smart_app (id)
);