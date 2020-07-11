@import ore.permission.role.{Role, RoleCategory}
@(roleCategory: RoleCategory, form: String = "form-continue", id: String = "", classes: String = "",
        hidden: Boolean = false)

@roles = @{
    Role.values.filter(role => role.category == roleCategory && role.isAssignable)
            .toList.sortBy(_.permissions: Long).reverse
}

<select id="@id" form="@form" class="@classes" @if(hidden) { style="display: none;" }>
    @for(roleType <- roles) {
        <option value="@roleType.value">@roleType.title</option>
    }
</select>
