-module(ping_server_db).
-export([start/1, init_db/0, loop/1]).

init_db() ->
    mnesia:create_schema([node()]),
    mnesia:start(),
    mnesia:create_table(messages, [
        {attributes, [id, sender, text, timestamp]},
        {disc_copies, [node()]}
    ]),
    io:format("[PingApp DB] تم إنشاء المستودع وقاعدة البيانات بنجاح!~n").

start(Port) ->
    init_db(),
    {ok, Socket} = gen_tcp:listen(Port, [binary, {packet, 0}, {active, false}, {reuseaddr, true}]),
    io:format("[PingApp Server] اشتغل وعم يستمع على البورت ~p...~n", [Port]),
    accept_clients(Socket).

accept_clients(ListenSocket) ->
    {ok, ClientSocket} = gen_tcp:accept(ListenSocket),
    spawn(fun() -> loop(ClientSocket) end),
    accept_clients(ListenSocket).

loop(Socket) ->
    case gen_tcp:recv(Socket, 0) of
        {ok, Packet} ->
            MessageText = binary_to_list(Packet),
            mnesia:transaction(fun() ->
                MsgId = erlang:unique_integer([positive]),
                Record = {messages, MsgId, "الـ GOAT", MessageText, erlang:system_time(millisecond)},
                mnesia:write(Record)
            end),
            io:format("[PingApp Log] وصلت رسالة وحُفظت: ~s~n", [MessageText]),
            gen_tcp:send(Socket, list_to_binary("تم استلام وحفظ رسالتك في PingApp: " ++ MessageText)),
            loop(Socket);
        {error, _} ->
            io:format("[PingApp Server] انقطع اتصال أحد الأجهزة.~n"),
            ok
    end.
