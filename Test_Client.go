package main

import (
	"net"
	"log"
	"encoding/json"
)

func main() {
	conn, err := net.Dial("tcp", ":8675")
	if err != nil {
		log.Fatal(err)
	}

	_, err = conn.Write([]byte("GET /connect HTTP/1.0\r\n\r\n"))
	if err != nil {
		log.Fatal(err)
	}

	msg := new(ServerMessage)
	msg.Status = ActionLogIn
	msg.Username = new(string)
	*msg.Username = "test_user"
	msg.Password = new(string)
	*msg.Password = "test_password"

	bytes, err := json.Marshal(msg)
	if err != nil {
		log.Fatal(err)
	}
	log.Printf("encoded message: %s", bytes)

	_, err = conn.Write(bytes)
	if err != nil {
		log.Fatal(err)
	}

	log.Printf("Sent %+v to server\n", *msg)

	rsp := new(ServerMessage)
	json.NewDecoder(conn).Decode(rsp)

	log.Printf("Received %+v from server\n", *rsp)
}