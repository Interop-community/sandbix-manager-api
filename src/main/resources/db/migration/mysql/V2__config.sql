# initialize config table

INSERT INTO config (key_name, value, config_type) VALUES
  ('Patient_1', 'Patient', 0),
  ('Patient_2', 'Patient?name=s', 0),
  ('Default_Patient_3', 'Patient?birthdate=>2010-01-01&birthdate=<2011-12-31', 0),
  ('Observation_1', 'Observation', 0),
  ('Observation_2', 'Observation?code=8480-6', 0),
  ('Default_Observation_3', 'Observation?category=vital-signs', 0),
  ('Observation_4', 'Observation?date=>2010-01-01&date=<2011-12-31', 0),
  ('Condition_1', 'Condition', 0),
  ('Condition_2', 'Condition?onset=>2010-01-01&onset=<2011-12-31', 0),
  ('Default_Condition_3', 'Condition?code:text=diabetes', 0),
  ('Procedure_1', 'Procedure', 0),
  ('Default_Procedure_2', 'Procedure?date=>2010-01-01&date=<2011-12-31', 0),
  ('AllergyIntolerance_1', 'AllergyIntolerance', 0),
  ('Default_AllergyIntolerance_2', 'AllergyIntolerance?date=>1999-01-01&date=<2011-12-31', 0),
  ('HSPC with Synthea', 'https://api3.hspconsortium.org/HSPCplusSynthea/open', 2),
  ('HSPC Test', 'https://api-stu3.hspconsortium.org/HSPCwithSynthea/open', 2);
