#import "MGLGeometry.h"

#import <TargetConditionals.h>
#if TARGET_OS_IPHONE
    #import <UIKit/UIKit.h>
#endif

#import <mbgl/util/geo.hpp>
#import <mbgl/util/geometry.hpp>

typedef double MGLLocationRadians;
typedef double MGLRadianDistance;
typedef double MGLRadianDirection;

/** Defines the coordinate by a `MGLRadianCoordinate2D`. */
typedef struct MGLRadianCoordinate2D {
    MGLLocationRadians latitude;
    MGLLocationRadians longitude;
} MGLRadianCoordinate2D;

/**
 Creates a new `MGLRadianCoordinate2D` from the given latitudinal and longitudinal.
 */
NS_INLINE MGLRadianCoordinate2D MGLRadianCoordinate2DMake(MGLLocationRadians latitude, MGLLocationRadians longitude) {
    MGLRadianCoordinate2D radianCoordinate;
    radianCoordinate.latitude = latitude;
    radianCoordinate.longitude = longitude;
    return radianCoordinate;
}

/// Returns the smallest rectangle that contains both the given rectangle and
/// the given point.
CGRect MGLExtendRect(CGRect rect, CGPoint point);

mbgl::LatLng MGLLatLngFromLocationCoordinate2D(CLLocationCoordinate2D coordinate);

NS_INLINE mbgl::Point<double> MGLPointFromLocationCoordinate2D(CLLocationCoordinate2D coordinate) {
    return mbgl::Point<double>(coordinate.longitude, coordinate.latitude);
}

NS_INLINE CLLocationCoordinate2D MGLLocationCoordinate2DFromPoint(mbgl::Point<double> point) {
    return CLLocationCoordinate2DMake(point.y, point.x);
}

NS_INLINE CLLocationCoordinate2D MGLLocationCoordinate2DFromLatLng(mbgl::LatLng latLng) {
    return CLLocationCoordinate2DMake(latLng.latitude(), latLng.longitude());
}

NS_INLINE MGLCoordinateBounds MGLCoordinateBoundsFromLatLngBounds(mbgl::LatLngBounds latLngBounds) {
    return MGLCoordinateBoundsMake(MGLLocationCoordinate2DFromLatLng(latLngBounds.southwest()),
                                   MGLLocationCoordinate2DFromLatLng(latLngBounds.northeast()));
}

NS_INLINE mbgl::LatLngBounds MGLLatLngBoundsFromCoordinateBounds(MGLCoordinateBounds coordinateBounds) {
    return mbgl::LatLngBounds::hull(MGLLatLngFromLocationCoordinate2D(coordinateBounds.sw),
                                    MGLLatLngFromLocationCoordinate2D(coordinateBounds.ne));
}

#if TARGET_OS_IPHONE
NS_INLINE mbgl::EdgeInsets MGLEdgeInsetsFromNSEdgeInsets(UIEdgeInsets insets) {
    return { insets.top, insets.left, insets.bottom, insets.right };
}
#else
NS_INLINE mbgl::EdgeInsets MGLEdgeInsetsFromNSEdgeInsets(NSEdgeInsets insets) {
    return { insets.top, insets.left, insets.bottom, insets.right };
}
#endif

/** Converts a map zoom level to a camera altitude.

    @param zoomLevel The zoom level to convert.
    @param pitch The camera pitch, measured in degrees.
    @param latitude The latitude of the point at the center of the viewport.
    @param size The size of the viewport.
    @return An altitude measured in meters. */
CLLocationDistance MGLAltitudeForZoomLevel(double zoomLevel, CGFloat pitch, CLLocationDegrees latitude, CGSize size);

/** Converts a camera altitude to a map zoom level.

    @param altitude The altitude to convert, measured in meters.
    @param pitch The camera pitch, measured in degrees.
    @param latitude The latitude of the point at the center of the viewport.
    @param size The size of the viewport.
    @return A zero-based zoom level. */
double MGLZoomLevelForAltitude(CLLocationDistance altitude, CGFloat pitch, CLLocationDegrees latitude, CGSize size);

/** Returns MGLRadianCoordinate2D, converted from CLLocationCoordinate2D. */
NS_INLINE MGLRadianCoordinate2D MGLRadianCoordinateFromLocationCoordinate(CLLocationCoordinate2D locationCoordinate) {
    return MGLRadianCoordinate2DMake(MGLRadiansFromDegrees(locationCoordinate.latitude),
                                     MGLRadiansFromDegrees(locationCoordinate.longitude));
}

/*
 Returns the distance in radians given two coordinates.
 */
NS_INLINE MGLRadianDistance MGLDistanceBetweenRadianCoordinates(MGLRadianCoordinate2D from, MGLRadianCoordinate2D to)
{
    double a = pow(sin((to.latitude - from.latitude) / 2), 2)
                + pow(sin((to.longitude - from.longitude) / 2), 2) * cos(from.latitude) * cos(to.latitude);
    
    return 2 * atan2(sqrt(a), sqrt(1 - a));
}

/*
 Returns direction in radians given two coordinates.
 */
NS_INLINE MGLRadianDirection MGLRadianCoordinatesDirection(MGLRadianCoordinate2D from, MGLRadianCoordinate2D to) {
    double a = sin(to.longitude - from.longitude) * cos(to.latitude);
    double b = cos(from.latitude) * sin(to.latitude)
                - sin(from.latitude) * cos(to.latitude) * cos(to.longitude - from.longitude);
    return atan2(a, b);
}

/*
 Returns coordinate at a given distance and direction away from coordinate.
 */
NS_INLINE MGLRadianCoordinate2D MGLRadianCoordinateAtDistanceFacingDirection(MGLRadianCoordinate2D coordinate,
                                                                             MGLRadianDistance distance,
                                                                             MGLRadianDirection direction) {
    double otherLatitude = asin(sin(coordinate.latitude) * cos(distance)
                                + cos(coordinate.latitude) * sin(distance) * cos(direction));
    double otherLongitude = coordinate.longitude + atan2(sin(direction) * sin(distance) * cos(coordinate.latitude),
                                                         cos(distance) - sin(coordinate.latitude) * sin(otherLatitude));
    return MGLRadianCoordinate2DMake(otherLatitude, otherLongitude);
}

/** Returns the coordinate at a given fraction between two coordinates.
 
 @param origin Origin `CLLocationCoordinate2D`.
 @param destination Destination `CLLocationCoordinate2D`.
 @param fraction Fraction between coordinates (0 = origin, 0.5 = middle, 1 = destination).
 
 @return A coordinate at given fraction. */
// Ported from http://www.movable-type.co.uk/scripts/latlong.html
NS_INLINE CLLocationCoordinate2D MGLCLCoordinateAtFraction(CLLocationCoordinate2D origin, CLLocationCoordinate2D destination, double fraction) {
    double φ1 = MGLRadiansFromDegrees(origin.latitude);
    double λ1 = MGLRadiansFromDegrees(origin.longitude);
    
    double φ2 = MGLRadiansFromDegrees(destination.latitude);
    double λ2 = MGLRadiansFromDegrees(destination.longitude);
    double sinφ1 = sin(φ1);
    double cosφ1 = cos(φ1);
    double sinλ1 = sin(λ1);
    double cosλ1 = cos(λ1);
    double sinφ2 = sin(φ2);
    double cosφ2 = cos(φ2);
    double sinλ2 = sin(λ2);
    double cosλ2 = cos(λ2);
    
    // distance between points
    double Δφ = φ2 - φ1;
    double Δλ = λ2 - λ1;
    double a = sin(Δφ/2) * sin(Δφ/2) + cos(φ1) * cos(φ2) * sin(Δλ/2) * sin(Δλ/2);
    double δ = 2 * atan2(sqrt(a), sqrt(1-a));
    
    double A = sin((1-fraction)*δ) / sin(δ);
    double B = sin(fraction*δ) / sin(δ);
    
    double x = A * cosφ1 * cosλ1 + B * cosφ2 * cosλ2;
    double y = A * cosφ1 * sinλ1 + B * cosφ2 * sinλ2;
    double z = A * sinφ1 + B * sinφ2;
    
    double φ3 = atan2(z, sqrt(x*x + y*y));
    double λ3 = atan2(y, x);
    
    CLLocationDegrees latitude = MGLDegreesFromRadians(φ3);
    CLLocationDegrees longitude = fmod(MGLDegreesFromRadians(λ3), 360 - 180);
    
    return CLLocationCoordinate2DMake(latitude, longitude);
}
