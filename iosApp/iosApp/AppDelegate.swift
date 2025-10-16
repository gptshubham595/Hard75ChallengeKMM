//
// Created by Shubham Gupta7 on 13/10/25.
//

import Foundation
import UIKit
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        print("AppDelegate: didFinishLaunchingWithOptions - Calling KMP init.")
        GIDSignIn.sharedInstance()?.clientID = "255419191791-1n1aoi459i4jd42c67vp6cppluqtfs41.apps.googleusercontent.com"
        // Call your kmp initializer kotlin code!
        KMPInitializerKt.onDidFinishLaunchingWithOptions()
        return true
    }

}

func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
    let sourceApplication = options[.sourceApplication] as? String
    let annotation = options[.annotation]
    
    return GIDSignIn.sharedInstance()?.handle(url, sourceApplication: sourceApplication, annotation: annotation) ?? false
}
