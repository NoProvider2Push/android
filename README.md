# NoProvider2Push

NoProvider2Push is a [UnifiedPush](https://github.com/UnifiedPush) distributor able to provide push notifications without a push provider. For this, it needs a static address even with the roaming. It is achievable with a custom network such as a VPN (eg. wireguard) or the yggdrasil network.

Push providers were introduced because mobile phones are always changing there IP address or behind a firewall. Being constantly connected to a server, named a push provider, is a solution and having a static address is another one. It can be achieved with a custom network.

## Address

The address is the phone static address on the custom network. 

It is necessary to know where to forward the notifications.

## Proxy

The proxy is able to forward requests inside the custom network, to reach the phone from the Internet.

Cf [Server-Side Proxy](#server-side-proxy) to configure your own proxy using nginx or [Enqueued Proxy](https://github.com/NoProvider2Push/enqueued-proxy) to not lose any message in case of deconnection.

## Server-side Proxy

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

Here, the port is fixed, because 1. Otherwise it would expose every ports (localhost included) to the internet ; 2. The port is always the same inside the app.

## To the app developers

If you are a user, you do not need to look here.

This application is a UnifiedPush distributor. If you want to use this to have push notifications on your app, or just to allow users to use this to have push notifications, you will need to embedded the [UnifiedPush library](https://github.com/UnifiedPush/UP-lib) in your application.
