
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "exportedGoogleMapsBridge",
    platforms: [.iOS("17.0"),.macOS("10.13"),.tvOS("12.0"),.watchOS("4.0")],
    products: [
        .library(
            name: "exportedGoogleMapsBridge",
            type: .static,
            targets: ["exportedGoogleMapsBridge"])
    ],
    dependencies: [
        .package(url: "https://github.com/googlemaps/ios-maps-sdk", exact: "10.6.0")
    ],
    targets: [
        .target(
            name: "exportedGoogleMapsBridge",
            dependencies: [
                .product(name: "GoogleMaps", package: "ios-maps-sdk")
            ],
            path: "Sources"
            
        )
        
    ]
)
        