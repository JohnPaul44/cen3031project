package main

import (
	"time"
	"sync"
	"net"
)

type Connection struct {
	conn net.Conn  // interface for reading and writing to the connection
	time time.Time // time that the connection was established (used for differentiating different connections with same username)
}

type UserConnection struct {
	profile     *Profile
	connections map[time.Time]*Connection
}

type Connections map[string]UserConnection

var conns = make(Connections)
var connsMutex = new(sync.Mutex)

func (conns Connections) contains(username string) bool {
	_, contains := conns[username]
	return contains
}

func (conns *Connections) add(user *DSUser) {
	connsMutex.Lock()
	_, contains := (*conns)[user.username]
	// user is not already in map, so create UserConnection object, set profile and initialize map
	if !contains {
		(*conns)[user.username] = UserConnection{user.Profile, make(map[time.Time]*Connection)}
	}
	// add connection to connections map
	(*conns)[user.username].connections[user.connection.time] = user.connection
	connsMutex.Unlock()

	// user was not previously in map, so update online status
	if !contains {
		updateOnlineStatus(user, true)
	}
}

func (conns *Connections) remove(user *DSUser) {
	connsMutex.Lock()
	delete((*conns)[user.username].connections, user.connection.time)
	if len((*conns)[user.username].connections) == 0 {
		delete(*conns, user.username)
	}
	_, contains := (*conns)[user.username]
	connsMutex.Unlock()

	// user is not in map anymore
	if !contains {
		updateOnlineStatus(user, false)
	}
}

func (conns *Connections) updateProfile(user *DSUser, profile Profile) {
	connsMutex.Lock()
	p := (*conns)[user.username].profile
	*p = profile
	*user.Profile = profile
	connsMutex.Unlock()
}
