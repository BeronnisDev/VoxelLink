-- Open a file in the connected external editor (requires VoxelLink mod + editor client).
local args = { ... }

if #args == 0 then
    print("Usage: vledit <file>")
    return
end

local path = shell.resolve(args[1])
local ok, err = voxellink.open(path)
if ok then
    print("Requested editor open for " .. path)
else
    print("vledit: " .. tostring(err))
end
