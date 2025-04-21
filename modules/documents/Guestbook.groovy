package documents

import spaceport.computer.alerts.Alert
import spaceport.computer.alerts.results.Result
import spaceport.computer.memory.physical.Document
import spaceport.computer.memory.physical.ViewDocument


/**
 * The Guestbook class represents a guestbook document in the database.
 * It contains information about the event and the participants who
 * signed the guestbook.
 *
 */
class Guestbook extends Document {

    def type = 'guestbook' // Type of document, used for views


    //
    // Static methods are called when the class is loaded
    //

    @Alert('on initialized')
    static _view(Result r) {

        // Create a 'view' in the database that lists all of the guestbooks
        // and their participants
        ViewDocument.get('views', 'guestbooks')
                .setViewIfNeeded('all', '''

            function(doc) {
                if (doc.type == 'guestbook') {
                    // Only need the cookie of the participant for this view
                    const participantCookies = doc.participants
                        .map(participant => participant.cookie);
                    emit(doc.info.owner_cookie, { 
                        info: doc.info, 
                        participants: participantCookies
                    });
                }
            }
        ''')
    }


    //
    // Instance properties and methods apply to each instance of the class
    //


    // Define the custom properties that will be serialized to the database
    // when the Document is saved/updated.
    def customProperties = [ 'info', 'participants' ]


    // Define a schema for the guestbook info
    static class GuestbookInfoSchema {
        String name         // Event name
        String email        // Event owner email
        String owner_cookie // Event owner UUID
        Boolean open        // Whether the guestbook can be signed (true) or not (false)
    }


    // Define a schema for the participants
    static class ParticipantSchema {
        String name          // Participant name
        String email         // Participant email
        Boolean public_email // Whether the email is public (true) or not (false)
        String message       // Participant message
        Long time_signed     // Time signed
        String cookie        // Participant UUID
    }


    // Participants are the people who signed the guestbook
    List<ParticipantSchema> participants = []


    // Info is the information about the event
    GuestbookInfoSchema info = new GuestbookInfoSchema()


    // Add a participant to the guestbook
    void addParticipant(String name, String email, Boolean public_email, String message, String cookie) {

        if (!isOpen()) return // If the guestbook is not open, do not add a participant

        def participant = new ParticipantSchema()
                .tap {
            it.name = name.clean()    // Clean String variables to prevent XSS
            it.message = message.clean()
            it.email = email.clean()
            it.public_email = public_email
            it.time_signed = System.currentTimeMillis()
            it.cookie = cookie.clean()
        }

        participants.add(participant)
        save()         // Perform a save to the database
        this._update() // Provide an update for reactivity
    }


    // Check if a participant is already in the guestbook
    boolean hasParticipant(String cookie) {
        return participants.find { it.cookie == cookie } != null
    }


    void removeParticipant(String cookie) {
        participants.removeIf { it.cookie == cookie }
        save()         // Perform a save to the database
        this._update() // Provide an update for reactivity
    }


    // Check if the guestbook is owned by the user
    boolean isOwner(String cookie) {
        return info.owner_cookie == cookie
    }


    // Check if the guestbook is open for signing
    boolean isOpen() {
        return info.open
    }


}
