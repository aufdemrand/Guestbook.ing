import spaceport.bridge.Command
import spaceport.computer.alerts.Alert
import spaceport.computer.alerts.results.HttpResult
import spaceport.launchpad.Launchpad

/**
 *
 * Guestbook application
 *
 * This application is a simple guestbook SaaS that allows
 * users to create events that can be signed by guests.
 *
 * This project serves as a sample application for Spaceport.
 *
 */
class App {

    
    // Redirect to index.html
    @Alert('on / hit')
    static _onHit(HttpResult r) {
        r.setRedirectUrl('index.html')
    }

    
    // Launchpad is Spaceport's templating engine that provides the ability to
    // make pages dynamic with features like variablot interpolation, server
    // actions, server-side elements, and property reactivity
    static def launchpad = new Launchpad()


    // The index page will serve as the main page, allowing users to create 
    // new events, and also view events they have interacted with.
    @Alert('on /index.html hit')
    static _index(HttpResult r) {
        launchpad.assemble([ 'index.ghtml', 'history.ghtml' ]).launch(r, '_wrapper.ghtml')
    }


    // This route will serve as the view into the guestbook by using a regular-expression
    // routing alert, matching the guestbook ID in the URL with a match group.
    @Alert('~on /g/(.*) hit')
    static _event(HttpResult r) {
        // r.matches[0] is now available to the template
        launchpad.assemble([ 'guestbook.ghtml' ]).launch(r, '_wrapper.ghtml')
    }


}
