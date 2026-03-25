<template>
  <div class="space-y-6">
    <div class="border-b border-gray-200 pb-6">
      <h1 class="app-page-title">Willkommen {{ displayName }}</h1>
      <p class="app-muted-text mt-2">Willkommen! Hier ist dein persoenlicher Ueberblick.</p>
    </div>

    <div class="space-y-6">
      <section class="bg-white rounded-lg shadow p-6 space-y-5">
        <h2 class="app-section-title">Gruppen</h2>

        <div>
          <h3 class="app-subsection-title mb-3">Aktive Gruppen</h3>
          <div class="overflow-x-auto">
            <table class="app-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Beschreibung</th>
                  <th>Gruppenleiter</th>
                  <th>Anzahl Mitglieder</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="isLoadingGroups">
                  <td colspan="4" class="app-table-cell-muted">Lade Gruppen...</td>
                </tr>
                <tr v-else-if="errorGroups">
                  <td colspan="4" class="app-table-cell-error">{{ errorGroups }}</td>
                </tr>
                <tr v-else-if="myGroups.length === 0">
                  <td colspan="4" class="app-table-cell-muted">Keine aktiven Gruppen vorhanden.</td>
                </tr>
                <tr
                  v-for="(group, index) in myGroups"
                  v-else
                  :key="String(readField(group, ['id', 'groupId', 'name']) ?? index)"
                >
                  <td class="app-table-cell-main">{{ displayGroupName(group) }}</td>
                  <td>{{ displayGroupDescription(group) }}</td>
                  <td>{{ displayGroupLeader(group) }}</td>
                  <td>{{ displayMemberCount(group) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <h3 class="app-subsection-title mb-3">Gruppeneinladungen</h3>
          <div class="overflow-x-auto">
            <table class="app-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Eingeladen durch</th>
                  <th>Aktionen</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="isLoadingInvitations">
                  <td colspan="3" class="app-table-cell-muted">Lade Gruppeneinladungen...</td>
                </tr>
                <tr v-else-if="errorInvitations">
                  <td colspan="3" class="app-table-cell-error">{{ errorInvitations }}</td>
                </tr>
                <tr v-else-if="groupInvitations.length === 0">
                  <td colspan="3" class="app-table-cell-muted">Keine Gruppeneinladungen vorhanden.</td>
                </tr>
                <tr
                  v-for="(invitation, index) in groupInvitations"
                  v-else
                  :key="String(readField(invitation, ['id', 'invitationId', 'groupInvitationId']) ?? index)"
                >
                  <td class="app-table-cell-main">{{ displayInvitationName(invitation) }}</td>
                  <td>{{ displayInvitationInviter(invitation) }}</td>
                  <td>
                    <div class="flex items-center gap-2">
                      <button
                        type="button"
                        class="app-btn-success-sm"
                        @click="handleAcceptInvitation(invitation)"
                      >
                        Akzeptieren
                      </button>
                      <button
                        type="button"
                        class="app-btn-danger-sm"
                        @click="handleDeclineInvitation(invitation)"
                      >
                        Ablehnen
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section class="bg-white rounded-lg shadow p-6 space-y-5">
        <h2 class="app-section-title">Aktivitaeten</h2>

        <div>
          <h3 class="app-subsection-title mb-3">Anstehende Aktivitaeten</h3>
          <MyActivitiesCard
            :activities="upcomingActivities"
            :is-loading="isLoadingActivities"
            :error="errorActivities"
          />
        </div>

        <div>
          <h3 class="app-subsection-title mb-3">Einladungen zu Aktivitaeten</h3>
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
  acceptInvitation,
  declineInvitation
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

const asRecord = (value: unknown): Record<string, unknown> =>
  value !== null && typeof value === 'object' ? (value as Record<string, unknown>) : {}

const readField = (group: unknown, keys: string[]): unknown => {
  const item = asRecord(group)
  for (const key of keys) {
    const value = item[key]
    if (value !== undefined && value !== null && value !== '') return value
  }
  return undefined
}

const displayGroupName = (group: unknown): string =>
  String(readField(group, ['name', 'groupName', 'title']) ?? '-')

const displayGroupDescription = (group: unknown): string =>
  String(readField(group, ['description', 'groupDescription', 'desc']) ?? '-')

const displayGroupLeader = (group: unknown): string =>
  String(readField(group, ['groupLeader', 'leaderName', 'ownerName', 'createdByName']) ?? '-')

const displayMemberCount = (group: unknown): string => {
  const directCount = readField(group, ['memberCount', 'membersCount', 'numberOfMembers'])
  if (directCount !== undefined) return String(directCount)

  const members = readField(group, ['members', 'memberList'])
  if (Array.isArray(members)) return String(members.length)

  return '-'
}

const displayInvitationName = (invitation: unknown): string =>
  String(readField(invitation, ['groupName', 'name', 'title']) ?? '-')

const displayInvitationInviter = (invitation: unknown): string =>
  String(readField(invitation, ['invitedByName', 'inviterName', 'createdByName', 'groupLeader']) ?? '-')

const getInvitationId = (invitation: unknown): number | null => {
  const candidate = readField(invitation, ['id', 'invitationId', 'groupInvitationId'])
  const parsed = Number(candidate)
  return Number.isFinite(parsed) ? parsed : null
}

const handleAcceptInvitation = async (invitation: unknown) => {
  const invitationId = getInvitationId(invitation)
  if (invitationId === null) return
  await acceptInvitation(invitationId)
}

const handleDeclineInvitation = async (invitation: unknown) => {
  const invitationId = getInvitationId(invitation)
  if (invitationId === null) return
  await declineInvitation(invitationId)
}
</script>
