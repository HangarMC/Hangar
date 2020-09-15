<template>
    <div>
        <div v-show="loading">
            <i class="fas fa-spinner fa-spin"></i>
            <span>Loading platforms for you...</span>
        </div>
        <div v-show="!loading && selectedPlatform == null">
            <span v-for="platform in platforms" @click="select(platform)" style="cursor: pointer;">
                <Tag :name="platform.name" :color="platform.tag" />
            </span>
        </div>
        <div v-if="!loading && selectedPlatform != null">
            <span @click="unselect">
                <i class="fas fa-times-circle"></i>
            </span>
            <input type="hidden" name="platform" :value="selectedPlatform.id" form="form-publish">
            <Tag :name="selectedPlatform.name" :color="selectedPlatform.tag" />
            <div>
                <template v-for="(v, index) in selectedPlatform.possibleVersions">
                    <label :for="'version-' + v" style="margin-left: 10px;">{{ v }}</label>
                    <input form="form-publish" :id="'version-' + v" type="checkbox" name="versions" :value="v">
                </template>
            </div>
        </div>
    </div>
</template>
<script>
import Tag from './components/Tag';

export default {
    name: 'platform-choice',
    components: {
        Tag
    },
    data() {
        return {
            platforms: [],
            loading: true,
            selectedPlatform: null
        }
    },
    methods: {
      select: function(platform) {
          this.selectedPlatform = platform;
      },
      unselect: function() {
          this.selectedPlatform = null;
      }
    },
    created() {
        var self = this;
        $.ajax({
            url: '/api/v1/platforms',
            dataType: 'json',

            complete: function() {
                self.loading = false;
            },

            success: function(platforms) {
                self.platforms = platforms;
            }
        })
    }
}
</script>