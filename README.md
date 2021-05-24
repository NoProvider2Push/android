# NoProvider2Push

NoProvider2Push is a [UnifiedPush](https://github.com/UnifiedPush) distributor able to provide push notifications without a push provider. For this, it needs a static address even with the roaming. It is achievable with a custom network such as a VPN (eg. wireguard) or the yggdrasil network.

Push providers were introduced because mobile phones are always changing there IP address or behind a firewall. Being constantly connected to a server, named a push provider, is a solution and having a static address is another one. It can be achieved with a custom network.

NoProvider2Push is a pretty niche UnifiedPush distributor mostly useful for development purposes and advanced users at this moment.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/org.unifiedpush.distributor.noprovider2push/)

## Address

The address is the phone static address on the custom network. 

It is necessary to know where to forward the notifications.

## Proxy

The proxy is able to forward requests inside the custom network, to reach the phone from the Internet.

Cf [Server-Side Proxy](#server-side-proxy) to configure your own proxy.

## Server-side Proxy

If you are a user, and you already have a gateway, you do not need to look here. 

The gateway is only needed to expose the ports of the phones to Internet.

For instance, it can be done with a simple nginx configuration:

```
location ~ ^/proxy/allowed.address:51515/(.*)$ {
    proxy_pass                 http://allow.address:51515/$1;
    client_max_body_size        50M;
    # Force https
    if ($scheme = http) {
        rewrite ^ https://$server_name$request_uri? permanent;
     }
}
```

## To the app developers

If you are a user, you do not need to look here.

This application is a UnifiedPush distributor. If you want to use this to have push notifications on your app, or just to allow users to use this to have push notifications, you will need to embedded the [UnifiedPush library](https://unifiedpush.org/developers/android/) in your application.
