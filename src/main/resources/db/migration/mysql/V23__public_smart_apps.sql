INSERT INTO smart_app
(smart_app_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author, copy_type, sandbox_id)
VALUES
  (
    'hspc-bilirubin-risk-chart',
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
    'Intermountain Healthcare',
    'MASTER',
    'TempDstu2SMART'
  ),
  (
    'hspc-my-web-app',
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
    'HSPC',
    'MASTER',
    'TempDstu2SMART'
  ),
  (
    'cds-hooks-sandbox',
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
    'CDS Hooks',
    'MASTER',
    'TempDstu2SMART'
  );

INSERT INTO smart_app
(smart_app_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author, copy_type, sandbox_id)
VALUES
  (
    'hspc-bilirubin-risk-chart',
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
    'Intermountain Healthcare',
    'MASTER',
    'TempStu3SMART'
  ),
  (
    'hspc-my-web-app',
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
    'HSPC',
    'MASTER',
    'TempStu3SMART'
  ),
  (
    'cds-hooks-sandbox',
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
    'CDS Hooks',
    'MASTER',
    'TempStu3SMART'
  );

INSERT INTO smart_app
(smart_app_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author, copy_type, sandbox_id)
VALUES
  (
    'hspc-bilirubin-risk-chart',
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
    'Intermountain Healthcare',
    'MASTER',
    'TempStu3Synthea'
  ),
  (
    'hspc-my-web-app',
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
    'HSPC',
    'MASTER',
    'TempStu3Synthea'
  ),
  (
    'cds-hooks-sandbox',
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
    'CDS Hooks',
    'MASTER',
    'TempStu3Synthea'
  );

INSERT INTO smart_app
(smart_app_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author, copy_type, sandbox_id)
VALUES
  (
    'hspc-bilirubin-risk-chart',
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
    'Intermountain Healthcare',
    'MASTER',
    'TempEmpty'
  ),
  (
    'hspc-my-web-app',
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
    'HSPC',
    'MASTER',
    'TempEmpty'
  ),
  (
    'cds-hooks-sandbox',
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
    'CDS Hooks',
    'MASTER',
    'TempEmpty'
  );

INSERT INTO smart_app
(smart_app_id, manifest_url, client_id, owner_id, created_timestamp, visibility, sample_patients, info, brief_description, author, copy_type, sandbox_id)
VALUES
  (
    'hspc-bilirubin-risk-chart',
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
    'Intermountain Healthcare',
    'MASTER',
    'TempR4SMART'
  ),
  (
    'hspc-my-web-app',
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
    'HSPC',
    'MASTER',
    'TempR4SMART'
  ),
  (
    'cds-hooks-sandbox',
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
    'CDS Hooks',
    'MASTER',
    'TempR4SMART'
  );