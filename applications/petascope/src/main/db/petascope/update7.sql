--------------------------------------------------------------------------------
-- Update sequence for table ps_crs in petascope creation query (ticket #289) --
--------------------------------------------------------------------------------

SELECT pg_catalog.setval('ps_crs_id_seq', (SELECT MAX(id) + 1 FROM ps_crs), true);
