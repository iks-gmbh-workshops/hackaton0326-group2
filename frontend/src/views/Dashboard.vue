<template>
  <div class="space-y-6">
    <div class="border-b border-gray-200 pb-6">
      <h1 class="app-page-title">Willkommen {{ displayName }}</h1>
      <p class="app-muted-text mt-2">Willkommen! Hier ist dein persönlicher Überblick.</p>
    </div>

    <div class="space-y-6">
      <section class="bg-white rounded-lg shadow p-6 space-y-5">
        <h2 class="app-section-title">Gruppen</h2>

        <div>
          <h3 class="app-subsection-title mb-3">Aktive Gruppen</h3>
          <MyGroupsCard
            :groups="myGroups"
            :is-loading="isLoadingGroups"
            :error="errorGroups"
          />
        </div>

        <div>
          <h3 class="app-subsection-title mb-3">Gruppeneinladungen</h3>
          <GroupInvitationsCard
            :invitations="groupInvitations"
            :is-loading="isLoadingInvitations"
            :error="errorInvitations"
            @accept="acceptInvitation"
          />
        </div>
      </section>

      <section class="bg-white rounded-lg shadow p-6 space-y-5">
        <h2 class="app-section-title">Aktivitäten</h2>

        <div>
          <h3 class="app-subsection-title mb-3">Anstehende Aktivitäten</h3>
          <MyActivitiesCard
            :activities="upcomingActivities"
            :is-loading="isLoadingActivities"
            :error="errorActivities"
          />
        </div>

        <div>
          <h3 class="app-subsection-title mb-3">Einladungen zu Aktivitäten</h3>
          <PendingActivitiesCard
            :activities="pendingActivities"
            :is-loading="isLoadingPending"
            :error="errorPending"
            @accept="acceptActivity"
            @decline="declineActivity"
          />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useDashboardGroups } from '../composables/useDashboardGroups'
import { useDashboardActivities } from '../composables/useDashboardActivities'
import MyGroupsCard from '../components/Dashboard/MyGroupsCard.vue'
import GroupInvitationsCard from '../components/Dashboard/GroupInvitationsCard.vue'
import MyActivitiesCard from '../components/Dashboard/MyActivitiesCard.vue'
import PendingActivitiesCard from '../components/Dashboard/PendingActivitiesCard.vue'

const displayName = computed(() => localStorage.getItem('auth_display_name') || '')

const {
  myGroups,
  groupInvitations,
  isLoadingGroups,
  isLoadingInvitations,
  errorGroups,
  errorInvitations,
  acceptInvitation
} = useDashboardGroups()

const {
  pendingActivities,
  isLoadingActivities,
  isLoadingPending,
  errorActivities,
  errorPending,
  acceptActivity,
  declineActivity,
  upcomingActivities
} = useDashboardActivities()
</script>

