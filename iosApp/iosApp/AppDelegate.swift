//
// Created by Shubham Gupta7 on 13/10/25.
//

import Foundation
import UIKit
import ComposeApp
import GoogleSignIn


class AppDelegate: UIResponder, UIApplicationDelegate {
    func application(_ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {

        KMPInitializerKt.onDidFinishLaunchingWithOptions()
        
        // Now you can safely access GIDSignIn
        print("AppDelegate: Setting GIDSignIn.sharedInstance.clientID")
        
        return true
    }
    
    // It's also critical to add this function to handle the URL callback
    // after the user signs in and returns to your app.
    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
      return GIDSignIn.sharedInstance.handle(url)
    }
}
