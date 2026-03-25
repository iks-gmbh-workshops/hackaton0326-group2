<template>
  <div>
    <div v-if="isLoading" class="flex items-center justify-center py-8">
      <div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
    </div>

    <div v-else-if="error" class="rounded-lg border border-red-200 bg-red-50 p-4">
      <p class="text-red-800">{{ error }}</p>
    </div>

    <div v-else-if="invitations.length === 0" class="py-8 text-center">
      <p class="text-gray-500">Keine Gruppeneinladungen vorhanden.</p>
    </div>

    <div v-else class="grid gap-4 sm:grid-cols-2">
      <article
        v-for="(invitation, index) in invitations"
        :key="String(readField(invitation, ['id', 'invitationId', 'groupInvitationId']) ?? index)"
        class="rounded-lg border-2 border-blue-500 bg-white p-4"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <h4 class="text-base font-semibold text-gray-900">
              {{ displayInvitationName(invitation) }}
            </h4>
            <p class="clamp-2-lines mt-2 text-sm text-gray-700">
              {{ displayInvitationDescription(invitation) }}
            </p>
          </div>
          <button
            type="button"
            class="inline-flex h-8 w-8 items-center justify-center rounded-full bg-blue-600 text-base font-bold text-white transition hover:bg-blue-700"
            title="Einladung akzeptieren"
            aria-label="Einladung akzeptieren"
            @click="emitAccept(invitation)"
          >
            &#10003;
          </button>
        </div>
        <dl class="mt-4 space-y-1 text-sm text-gray-700">
          <div class="flex gap-2">
            <dt class="font-medium">Teilnehmer:</dt>
            <dd>{{ displayInvitationMemberCount(invitation) }}</dd>
          </div>
          <div class="flex gap-2">
            <dt class="font-medium">Gruppenleiter:</dt>
            <dd>{{ displayInvitationLeader(invitation) }}</dd>
          </div>
        </dl>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  invitations: unknown[]
  isLoading: boolean
  error: string | null
}

defineProps<Props>()

const emit = defineEmits<{
  accept: [invitationId: number]
}>()

const asRecord = (value: unknown): Record<string, unknown> =>
  value !== null && typeof value === 'object' ? (value as Record<string, unknown>) : {}

const readField = (item: unknown, keys: string[]): unknown => {
  const record = asRecord(item)
  for (const key of keys) {
    const value = record[key]
    if (value !== undefined && value !== null && value !== '') return value
  }
  return undefined
}

const displayInvitationName = (invitation: unknown): string =>
  String(readField(invitation, ['groupName', 'name', 'title']) ?? '-')

const displayInvitationDescription = (invitation: unknown): string =>
  String(readField(invitation, ['description', 'groupDescription', 'desc']) ?? '-')

const displayInvitationMemberCount = (invitation: unknown): string => {
  const directCount = readField(invitation, ['memberCount', 'membersCount', 'numberOfMembers'])
  if (directCount !== undefined) return String(directCount)

  const members = readField(invitation, ['members', 'memberList'])
  if (Array.isArray(members)) return String(members.length)

  return '-'
}

const displayInvitationLeader = (invitation: unknown): string =>
  String(
    readField(invitation, [
      'groupLeader',
      'leaderName',
      'ownerName',
      'createdByName',
      'invitedByName',
      'inviterName'
    ]) ?? '-'
  )

const getInvitationId = (invitation: unknown): number | null => {
  const candidate = readField(invitation, ['id', 'invitationId', 'groupInvitationId'])
  const parsed = Number(candidate)
  return Number.isFinite(parsed) ? parsed : null
}

const emitAccept = (invitation: unknown) => {
  const invitationId = getInvitationId(invitation)
  if (invitationId === null) return
  emit('accept', invitationId)
}
</script>

<style scoped>
.clamp-2-lines {
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
