import pg from "pg";

const {Pool} = pg;

const kratosPool = new Pool({
    connectionString: "postgresql://hangarauth:@localhost:1234/hangarauth",
    ssl: {rejectUnauthorized: false}
    //connectionString: "postgresql://hangar:hangar@localhost:5432/kratos",
});

const hangarPool = new Pool({
    connectionString: "postgresql://hangar:@localhost:1234/hangar",
    ssl: {rejectUnauthorized: false}
    //connectionString: "postgresql://hangar:hangar@localhost:5432/hangar",
});

const res = await kratosPool.query(`
    SELECT i.id                                                                                  uuid,
           i.traits ->> 'username'                                                               username,
           iva.status                                                                            email_verified,
           iva.value                                                                             email,
           ic.config                                                                             creds,
           ict.name                                                                              cred_type,
           ic.created_at                                                                         created_at,
           ic.updated_at                                                                         updated_at,
           json_build_object('github', i.traits ->> 'github', 'discord', i.traits ->> 'discord') socials
    FROM identities i
        JOIN identity_verifiable_addresses iva ON i.id = iva.identity_id
        JOIN identity_credentials ic ON i.id = ic.identity_id
        JOIN identity_credential_types ict ON ict.id = ic.identity_credential_type_id
    ORDER BY username
`);
console.log("queried " + res.rows.length + " rows!");

for (let row of res.rows) {
    console.log("migrating " + row.username + " " + row.cred_type);

    // first, check if user exists
    const user = await hangarPool.query(`
        SELECT id
        FROM users
        WHERE uuid = $1
    `, [row.uuid]);

    let userId = user?.rows?.[0]?.id;
    if (!userId) {
        console.warn("Unknown user " + row.username + ", creating", user.rows);
        const verified = row.email_verified === "completed";
        const insert = await hangarPool.query(`
            INSERT INTO users(uuid, created_at, name, email, join_date, email_verified, socials)
            VALUES ($1, $2, $3, $4, $5, $6, $7)
            RETURNING id
        `, [row.uuid, row.created_at, row.username, row.email, row.created_at, verified, row.socials]);
        userId = insert?.rows?.[0]?.id;
        if (!userId) {
            console.error("insert fucked?", row, insert);
            continue;
        }
    }

    try {
        await hangarPool.query("BEGIN");

        let type;
        if (row.cred_type === "password") {
            type = 0;
        } else if (row.cred_type === "lookup_secret") {
            type = 1;
        } else if (row.cred_type === "totp") {
            type = 2;
        } else if (row.cred_type === "webauthn") {
            type = 3;
        }

        await hangarPool.query(`
            INSERT INTO user_credentials(created_at, user_id, credential, type, updated_at)
            VALUES ($1, $2, $3, $4, $5)
            ON CONFLICT (user_id, type) DO UPDATE SET created_at = $1, credential = $3, updated_at = $5
        `, [row.created_at, userId, row.creds, type, row.updated_at]);

        const verified = row.email_verified === "completed";
        await hangarPool.query(`UPDATE users
                                SET email_verified = $1, socials = $2
                                WHERE id = $3`, [verified, row.socials, userId])

        await hangarPool.query("COMMIT");
    } catch (e) {
        await hangarPool.query("ROLLBACK");
        console.error("Failed to migrate " + row.username + " " + row.cred_type);
        console.error(e);
    }
}

await kratosPool.end();
