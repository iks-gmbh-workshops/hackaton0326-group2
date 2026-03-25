<template>
  <div>
    <div v-if="isLoading" class="flex items-center justify-center py-8">
      <div class="h-8 w-8 animate-spin rounded-full border-b-2 border-blue-600"></div>
    </div>

    <div v-else-if="error" class="rounded-lg border border-red-200 bg-red-50 p-4">
      <p class="text-red-800">{{ error }}</p>
    </div>

    <div v-else-if="groups.length === 0" class="py-8 text-center">
      <p class="text-gray-500">Keine aktiven Gruppen vorhanden.</p>
    </div>

    <div v-else class="grid gap-4">
      <article
        v-for="(group, index) in groups"
        :key="String(readField(group, ['id', 'groupId', 'name']) ?? index)"
        class="rounded-lg bg-blue-50 p-4"
      >
        <h4 class="text-base font-semibold text-gray-900">{{ displayGroupName(group) }}</h4>
        <p class="clamp-2-lines mt-2 text-sm text-gray-700">
          {{ displayGroupDescription(group) }}
        </p>
        <dl class="mt-4 space-y-1 text-sm text-gray-700">
          <div class="flex gap-2">
            <dt class="font-medium">Teilnehmer:</dt>
            <dd>{{ displayMemberCount(group) }}</dd>
          </div>
          <div class="flex gap-2">
            <dt class="font-medium">Gruppenleiter:</dt>
            <dd>{{ displayGroupLeader(group) }}</dd>
          </div>
        </dl>
      </article>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  groups: unknown[]
  isLoading: boolean
  error: string | null
}

defineProps<Props>()

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
</script>

<style scoped>
.clamp-2-lines {
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
</style>
