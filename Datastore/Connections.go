package Datastore

import (
	"net"
	"time"
	"sync"
	msg "../ServerMessage"
)

type Connection struct {
	Conn net.Conn  // interface for reading and writing to the connection
	Time time.Time // Time that the connection was established (used for differentiating different Connections with same username)
}

type userConnection struct {
	Profile     *msg.Profile
	Connections map[time.Time]*Connection
}

type connections map[string]userConnection

var conns connections
var connsMutex *sync.Mutex

func init() {
	conns = make(connections)
	connsMutex = new(sync.Mutex)
}

func ConnectionsContains(username string) bool {
	_, contains := conns[username]
	return contains
}

func AddConnection(user *User) bool {
	connsMutex.Lock()
	_, contains := conns[user.Username]

	// user is not already in map, so create userConnection object, set Profile and initialize map
	if !contains {
		profile := new(msg.Profile)
		*profile = user.Profile
		conns[user.Username] = userConnection{profile, make(map[time.Time]*Connection)}
	}

	// add connection to Connections map
	conns[user.Username].Connections[user.Connection.Time] = user.Connection
	connsMutex.Unlock()

	// user was not previously in map, so update online status
	return !contains
}

func RemoveConnection(user *User) bool {
	connsMutex.Lock()
	delete(conns[user.Username].Connections, user.Connection.Time)

	if len(conns[user.Username].Connections) == 0 {
		delete(conns, user.Username)
	}

	_, contains := conns[user.Username]
	connsMutex.Unlock()

	// user is not in map anymore
	return !contains
}

func GetConnections(username string) map[time.Time]*Connection {
	return conns[username].Connections
}

func GetConnectionProfile(username string) *msg.Profile {
	return conns[username].Profile
}

func UpdateConnectionProfile(user *User, profile msg.Profile) {
	connsMutex.Lock()
	p := conns[user.Username].Profile
	*p = profile
	user.Profile = profile
	connsMutex.Unlock()
}
