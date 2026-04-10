-- SmartFeather starter RLS policies for Supabase
-- Run in Supabase SQL editor after replacing table names/columns as needed.

-- 1) Enable RLS on each app table.
alter table if exists public.profiles enable row level security;
alter table if exists public.tasks enable row level security;
alter table if exists public.farms enable row level security;

-- 2) Profiles: users can only read/update their own row.
create policy if not exists "profiles_select_own"
on public.profiles
for select
using (auth.uid() = id);

create policy if not exists "profiles_update_own"
on public.profiles
for update
using (auth.uid() = id)
with check (auth.uid() = id);

-- 3) Tasks: allow users to read rows assigned to them.
create policy if not exists "tasks_select_assignee"
on public.tasks
for select
using (auth.uid() = assigned_user_id);

-- 4) Tasks: allow assignee to update status/notes on their own tasks.
create policy if not exists "tasks_update_assignee"
on public.tasks
for update
using (auth.uid() = assigned_user_id)
with check (auth.uid() = assigned_user_id);

-- 5) Farms: only farm members can read related data.
-- Assumes a join table public.farm_members(farm_id uuid, user_id uuid).
create policy if not exists "farms_select_members"
on public.farms
for select
using (
    exists (
        select 1
        from public.farm_members fm
        where fm.farm_id = farms.id
          and fm.user_id = auth.uid()
    )
);

-- Optional hardening: deny anon access by default at table level.
revoke all on table public.profiles from anon;
revoke all on table public.tasks from anon;
revoke all on table public.farms from anon;
