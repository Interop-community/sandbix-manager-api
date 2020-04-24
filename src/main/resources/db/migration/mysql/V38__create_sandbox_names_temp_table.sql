# Create a temporary table to hold the sandbox names and then clear it up in 2hrs

CREATE TABLE concurrent_sandbox_names (
  sandbox_name              VARCHAR(255)     DEFAULT NULL
);