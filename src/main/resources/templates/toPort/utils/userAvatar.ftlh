@(userName: Option[String], avatarUrl: String = "", imgSrc: String = null, clazz: String = "", attr: Map[String, String] = Map(), href: String = null)

@src = @{
    if (imgSrc == null) {
        if (userName.nonEmpty)
            avatarUrl
        else
            ""
    } else
        imgSrc
}

@url = @{
    if (href != null)
        href
    else if (userName.isEmpty)
        "#"
    else
        routes.Users.showProjects(userName.get).url
}

@name = @{
    if (userName.isEmpty)
        ""
    else
        userName
}

<a href="@url" @for((k, v) <- attr) { @(s"$k=$v") }>
    <img class="user-avatar @clazz" title="@name" src="@src" alt="@name" />
</a>
