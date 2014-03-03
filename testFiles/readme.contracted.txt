12/7/2002
Have gotten things to the point where I can use the Java 
implementation to do check-outs and check-ins.  I have the server
running on Chris' machine, and run the GUI client on my machine.
Check-out (of tip only), get (of tip only), visual compare 
(of tip only), and check-ins now all work pretty much as needed, 
though they all need some more polish to be ready for public 
consumption.  Performance is good.  I did some major re-factoring
to use a skinny representation of the LogFile object for remote
projects.

I did a rough performance comparison to QVCS-Pro, -- did a get
of all the files in the QVCS-Pro cppSrc/QWin4 directory.  The 
Java client to server (i.e. genuinely a remote get from my 
machine as client, and Chris' machine as server) took ~19 seconds
to fetch 335 files.

The C++ flavor of QVCS-Pro took ~20 seconds, where it actually
did not fetch any revisions from obsolete files -- meaning that
it actually did less work than the Java version.

This was done using the 1.4.1_01 JVM.

I've got compression enabled, and was also using a secure connection
to the server.  I haven't tried things on a non-secure connection,
though my guess is that it won't make much difference.


10/27/2002
Trying to get QVCS-Enterprise to the point that I can begin to
use it instead of the C++ flavor.

Some notes -- I need to be able to fire actions to occur on the
receipt of a server response.  The driving use case here is to make
the visual file compare button work for remote archives.  The problem
is that pressing the compare button can only start the process of
the compare -- namely send a 'get revision' request to the server.
The actual launch of the visual compare frame can't happen until
after the response from the server that contains the requested 
revision.  It's at that point that we can launch the visual compare.

So the way to do this is to create a 'TaskCache' keyed by some
number that we put into the request, and return in the response.
The cache also contains a 'Runnable' that we use to do the work
on receipt of the response from the server.  i.e. when we get the
response, we see if it contains a TaskCache index.  If it does, 
we fetch the 'Runnable' from the cache, and run it.  The guy who
populated the cache is responsible for figuring out what work
needs to run on receipt of the response.  This should work for 
the visual compare, and also turns into a fairly useful pattern
for any of the server responses.

06/30/02
Completed first pass at comprehensive testing of the compare
algorithm.  I can now have the Java code create an archive file,
add revisions to it, and then fetch the first revision, and have
that revision be identical to the original first revision of
the file.

There is a unit test that effectively gets all the trunk revisions from
a C++ created archive, and then creates (using the Java code), a
2nd archive (created entirely using the Java code).

This all works, and things are valid down to the byte level.  The
unit test is not exhaustive, per se, but it probably does a pretty
good coverage test -- it did find a couple of bugs in the code.

06/01/02
Now have creation of archive coded in LogFile and LogfileImpl along
with the unit test for it.

Still have to add support for defining the archive's attributes.
This needs to get passed on the command line.

At this point, I also have checkout, checkout, lock, and create
working... though they still have to be cleaned up to make sure
they are feature complete (right now they work, but don't 
support all the variations available in the C++ version of the
product).

05/13/2002
Fixed the workfile path problem.... conditionally append a path
separator character to the user's workfile location.

05/12/2002
Have the remote server sending the remote project node structure 
to the client... but have broken the workfile path of the local
(and remote?) projects so that (for example) a compare won't work
nor will a get work.  It's missing a path separator in between the
final two segments of the path.

05/02/2002
Changed the project tree GUI code to not auto-populate the tree
hierarchy if the project is remote.  This means that currently, remote
projects will only display the root node of the project, as I don't
yet have the implementation that would populate the rest of the
tree.

I'm thinking there should be three separate project classes: local
project, served project, and remote project.  A local project is local
to the GUI and is not 'served'. A 'served project' is used by the 
server, and is served. A 'remote project' is used by the GUI to
connect to a server that is serving a 'served project'.  The names
of a 'remote project' and a 'served project' must match, as that
is the way that they connect to each other, in addition to the
'remote project' needing to know the 'address' for the 'served
project' and how to connect to it.

The GUI will populate its tree using project property files that 
describe 'local projects' and 'remote projects'.  The server will populate
its notion of projects based on 'served projects'.

All this means that I need to create some classes to handle these
distinctions.  I'm guessing there can be a common base class that
handles most (if not all) of the implementation.  The biggest
distinction will be in how the associated property files are named.

05/01/2002
Need to add a 'add tree node' kind of message that flows from the 
server to the client.  It would consist of a String[] that contains
the hierarchy of tree segments, starting at the root node.

04/17/2002
Can now do a simple file get from the GUI.  Keyword expansion does
not work yet.... it's not implemented at all.

04/14/2002
Have an admin app roughed in.  Can now login via the Admin app,
and now the user must login as well.  This is handled silently for
now, as the username and password are stored in the server properties
file.  Arguably, this should be changed to remove the password 
from the properties file (at least).

03/09/2002
I have also written a unit test that goes through all the existing
C++ created archives and uses the C++ qget to retrieve the oldest
revision to a temp file.  The test then uses the Java code to get
the same revision into a separate temp file and then uses qdiff to
compare these two temp files.  To pass the test, the files should
be identical.

This test allowed me find some bugs.  At this writing, the test passes
for all the different archives that I have it go through.

03/09/2002
This past week, Brian has been home for Spring break.  I bought a
router (D-Link DI-704P) and a CD-RW drive.  Brian got both installed
and all now works as it should.

The new set-up means we don't have to have Chris' machine turned on
in order to access the internet.  Performance seems to be better
than before.  I hadn't realized that Chris' machine was a bottleneck.

03/09/2002
Finished writing a first cut at a Role Manager class.  This, along
with its associate store (RoleStore) provides role mapping so
we can assign roles to users, and test whether a given user is 
in a certain role.

What I don't have is any user interface for populating the store.
The idea is that the user authentication will be handled separately
by the Authenticator (or an AuthenticationManager).  The 
Authenticator establishes a user's identity.  The RoleManager
establishes the roles assigned to that identity.

There is a hard-coded ADMIN user to begin with.

02/22/2002
Starting to put user authentication stuff into the server side of
things.

What I want is to create user roles at the project level that are
'inherited' from a master list of roles -- i.e. very much like I did
in the old C++ code for project settings.  If a user is defined
in an ancestor of the current project, then the roles that were
defined above are inherited into the current project.  The roles
for the current project can be specialized for the current project
if desired.  The same idea applies to the user list -- it's 
inherited the same way.

It would be nice to make all this stuff factory driven and hidden
behind an interface so the factory could be configured to use a
different authenticator on a per project basis.

I've only begun to rough this in, and what I currently have does 
not implement the ideas described above.  The classes that are
affected include the new Authenticator class, the ClientRequestLogin
class, and the QVCSEnterpriseServer class.

02/20/2002
Have a rough version of the gui client working in client server mode.
It includes support for raw sockets, or SSL sockets (I've switched
to using the 1.4 JVM, since it will likely be the one I actually 
deploy on, and it includes bundled support for SSL).  The SSL that
I use is anonymous, so there's no need for certificates.

I've tested remotely with Bruce accessing the server running on the
Windows '98 box.  (That was before I switched to the 1.4 JVM).  I can't
get the 1.4 JVM to install on the Windows '98 box.  I'm assuming that
is a bug in the 1.4 release that will get fixed before I bother to
test in earnest over the net.

01/26/2002
Need to flesh out the code in ArchiveDirectoryManagerProxy so that
it returns useful information to its callers.  Right now, the
table model is throwing an exception because the class is returning
a -1 as the archive count.

01/26/2002
Roughing in the code to allow the GUI to talk to remote servers.
Getting the framework roughed in. There is still a good deal of
work to go.

The result will allow the GUI to have both local and remote projects
within the same display.  The project properties now have a 
property that points to a server name.  By default, the server
name is 'localhost'.  If the server name is localhost, then we'll
use this JVM's ArchiveDirectoryManger; if the server name is
something different, then we'll create a ArchiveDirectoryManagerProxy
and have it open up a connection to the remote server.

That much is roughed in.  I need to create some server property
files that allow things to go forward.

01/18/2002
TODO -- whenever I have a rename failure in LogFileImpl, I need to
re-read the logfile, since the member variables are out of sync with
the on-disk representation of the archive.

01/17/2002
Have gotten lockRevision to work -- or so it seems.  I have a unit
test that locks a non-tip revision in an archive that I took from
the existing java archives.  I'm beginning to rough in the unlockRevision
method, and want to begin work on checkinRevision.

The checkinRevision will be the trickiest in that I'll have to be
able to do a full write of an archive file from scratch.  As it is
today, I can update an existing archive 'in place', just overwriting
the archive header and the revision header.  For checkin to work
I'll have to compute the delta, add a revision to the archive, etc.
Not a trivial piece of work.

12/19/2001
Wrote a wrapper logfile class that makes use of the ReadWriteLock class
described below. The idea is that users of the logfile class will 
call methods on the wrapper which will acquire the kind of lock
needed for the requested operation, then call through to the
logfileimpl class that actually does the work. Since the lock
is acquired before we get into the logfileimpl class, we don't
have to worry within that class about collisions, etc.  Should
oughta work.

The next step is to add write methods to the classes that compose a
logfile so I can actually write some code that updates the archive.
i.e. before I can succeed at that, I need to update all these classes
so I can actually write a new archive file.

12/14/2001
Wrote a ReadWriteLock class to allow me to have multiple readers 
accessing a logfile object at the same time.  The idea is to use this
class to manage the locking of the potentially dynamic data of the 
logfile object. I want to allow multiple threads to have concurrent
read access to the logfile object, but allow only a single thread
to do writes/updates to the object. This class is where that 
concurrency is managed.

12/13/2001
Have a very rough client/server version of QVCS running.  I've created
a test server, and a test client, and am able to do a 'get revision'.
This 'get revision' results in copying a file revision from a QVCS archive
file on the server to a workfile on the client. I have tested on the W2K
machine as client and server and between the NT machine (as client) and
the W2K machine as server.

What I want to do next is put a read/write lock into the logfile class
so that I can have many reader threads traversing a single logfile; if a
write thread comes along, it must wait for all the reader threads to 
complete before proceeding.  When the write thread is active, all other
threads must be blocked for those operations that require access to 
any resources that may be undergoing change.

The goal here is to not bother with having to create the lock file that
I currently must do with the C++ implementation.  If there is only the
single logfile object associated with the actual logfile, then I should
be able to do all my 'locking' within that logfile instance without having
to use the file system.  It should improve performance quite a bit.  I can
get away with this since the architecture is client/server. This trick
is to guarantee that there is only ever a single logfile object per
archive file.

11/18/2000
Completed inclusion of new look toolbar buttons.

11/07/2000
I can display archive files in a file list display.  The tree control
side of the screen is also roughed in.

10/28/2000
Compression now works as well.  I've roughed in a QWin project.  It
still has a long way to go, but the rough outline of the GUI is in place.

10/09/2000
I got a simple revision fetch to work.  It can apply edits to retrieve
any revision from an archive.  The code is in the LogFile class in
the getRevision method.  I tested it using the qlogfile.dqq archive and
was able to fetch revision 1.0 and have it match the same revision 1.0
when fetched using the C++ tool.

This also works with compressed archives; i.e. I got the decompression
method to work also.

It also seems to work with branches.  The trick there is to make sure that
each revision in the archive knows who its parent revision is.... i.e.
the revision to which the revision's edits get applied in order to produce
the given revision.

This hasn't been thoroughly tested.  The branch I tested on had only a 
single revision on the branch.  A better test would include a branch
that had more than a single revision.
