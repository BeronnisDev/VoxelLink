-- Open a file in the connected external editor (requires CC Editor Bridge mod + editor client).
local args = { ... }

if #args == 0 then
    print("Usage: ccedit <file>")
    return
end

local path = shell.resolve(args[1])
local ok, err = cceditor.open(path)
if ok then
    print("Requested editor open for " .. path)
else
    print("ccedit: " .. tostring(err))
end
