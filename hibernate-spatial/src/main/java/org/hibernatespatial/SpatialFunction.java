package org.hibernatespatial;

/**
 * Spatial functions that users generally expect in a database.
 *
 * @author Karel Maesen, Geovise BVBA
 *         creation-date: Oct 7, 2010
 */
public enum SpatialFunction {

    dimension("SFS 1.1"),
    geometrytype("SFS 1.1"),
    srid("SFS 1.1"),
    envelope("SFS 1.1"),
    astext("SFS 1.1"),
    asbinary("SFS 1.1"),
    isempty("SFS 1.1"),
    issimple("SFS 1.1"),
    boundary("SFS 1.1"),
    equals("SFS 1.1"),
    disjoint("SFS 1.1"),
    intersects("SFS 1.1"),
    touches("SFS 1.1"),
    crosses("SFS 1.1"),
    within("SFS 1.1"),
    contains("SFS 1.1"),
    overlaps("SFS 1.1"),
    relate("SFS 1.1"),
    distance("SFS 1.1"),
    buffer("SFS 1.1"),
    convexhull("SFS 1.1"),
    intersection("SFS 1.1"),
    geomunion("SFS 1.1"), //is actually UNION but this conflicts with SQL UNION construct
    difference("SFS 1.1"),
    symdifference("SFS 1.1"),
    filter("Min. boundingbox overlap") //Minimum Bounding Rectangle overlaps
    ;

    private final String description;

    SpatialFunction(String specification) {
        this.description = specification;
    }

}
