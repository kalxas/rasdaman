CREATE TABLE ps_multipoint (
        id      serial  PRIMARY KEY,
        coverage_id integer NOT NULL,
        coordinate  geometry NOT NULL,
        value       numeric[] NOT NULL, --i.e., {r,g,b}
        -- Constraints and FKs
        --UNIQUE (coverage_id, coordinate),
        FOREIGN KEY (coverage_id) REFERENCES ps_domain_set (coverage_id) ON DELETE CASCADE
    );
CREATE INDEX coordinate_gist_idx ON ps_multipoint USING GIST(coordinate gist_geometry_ops_nd);
CREATE INDEX coverage_id_idx ON ps_multipoint (coverage_id);
