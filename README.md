# NoProvider2Push

NoProvider2Push is able to provide push notifications without a push provider. For this, it needs a static address even with the roaming. It is achievable with a custom network such as a VPN (eg. wireguard) or the yggdrasil network.

Push providers were introduced because mobile phones are always changing there IP address or behind a firewall. Being constantly connected to a server, named a push provider, is a solution and having a static address is another one. It can be achieved with a custom network.

## Address

The address is the phone static address on the custom network. It is necessary to know where to forward the notifications. For the moment it has to be manually enter inside the app.

## Gateway

The gateway is the proxy to forward requests inside the custom network, to reach the phone from the Internet.

cf [Serveur-Side Gateway](#server-side-gateway) to configure your own gateway.

## Server-side Gateway

If you are a user, and you already have a gateway, you do not need to look here. 

The gateway is only needed to expose the ports of the phones to Internet.

For instance, it can be done with a simple nginx configuration:

```
location ~ ^/proxy/(?<address>[^:]+):(?<port>[^/]+)/(.*)$ {
    proxy_pass                 http://$address:51515/$3;
    client_max_body_size        50M;
    # Force https
    if ($scheme = http) {
        rewrite ^ https://$server_name$request_uri? permanent;
     }
}
```

Here, the port is fixed, because 1. it would expose every ports (localhost included) to the internet ; 2. the port is always the same inside the app.

## To the app developpers

If you are a user, you do not need to look here.

If you want to use this to have push notifications on your app, or just to allow users to use this to have push notifications, you will need to embedded the [connector](https://github.com/NoProvider2Push/android-connector) in your application. You need to subscribe to push notification on the application server (for instance a messagerie server or a social media server) with the endpoint received during registration. You can additionnaly add some path (for instance `$endpoint/_needed_to_push`). The endpoint received during registration is composed like this `$user_gateway:$port/$application/` (for instance https://relay.example.tld/proxy/10.10.10.1:51515/com.flyingpanda.noprovider2pushtester/).
